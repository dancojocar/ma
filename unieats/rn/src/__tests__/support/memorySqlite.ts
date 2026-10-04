/// <reference types="node" />
import { DatabaseSync, type SQLInputValue } from "node:sqlite";

/**
 * Stand-in for expo-sqlite in Jest: the synchronous calls the app makes, run by Node's built-in SQLite on
 * in-memory databases. Use it with `jest.mock("expo-sqlite", () => jest.requireActual("./support/memorySqlite"))`.
 */
type Params = SQLInputValue[];

const files = new Map<string, DatabaseSync>();

const plain = <T>(row: object | undefined): T | null => (row ? ({ ...row } as T) : null);

export function wrap(raw: DatabaseSync) {
  return {
    execSync: (sql: string) => raw.exec(sql),
    getFirstSync: <T>(sql: string, params: Params = []) => plain<T>(raw.prepare(sql).get(...params)),
    getAllSync: <T>(sql: string, params: Params = []) =>
      raw.prepare(sql).all(...params).map((row) => ({ ...row }) as T),
    runSync: (sql: string, params: Params = []) => {
      const result = raw.prepare(sql).run(...params);
      return { changes: Number(result.changes), lastInsertRowId: Number(result.lastInsertRowid) };
    },
    withTransactionSync: (task: () => void) => {
      raw.exec("BEGIN");
      try {
        task();
        raw.exec("COMMIT");
      } catch (e) {
        raw.exec("ROLLBACK");
        throw e;
      }
    },
    closeSync: () => {},
  };
}

export function openDatabaseSync(name: string) {
  let raw = files.get(name);
  if (!raw) {
    raw = new DatabaseSync(":memory:");
    files.set(name, raw);
  }
  return wrap(raw);
}

/** Leaves `raw` "on disk" under `name`, as an older app version would have, for the next `openDatabaseSync`. */
export function placeDatabase(name: string, raw: DatabaseSync) {
  files.set(name, raw);
}
