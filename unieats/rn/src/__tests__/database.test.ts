/// <reference types="node" />
import { DatabaseSync } from "node:sqlite";
import type { SQLiteDatabase } from "expo-sqlite";
import { MIGRATIONS } from "../db/database";

jest.mock("expo-sqlite", () => jest.requireActual("./support/memorySqlite"));

/** A database as the app version with schema `version` left it (l07 = 1, l08 = 2, l10 = 3). */
function databaseAt(version: number): DatabaseSync {
  const raw = new DatabaseSync(":memory:");
  for (const sql of MIGRATIONS.slice(0, version)) raw.exec(sql);
  raw.exec(`PRAGMA user_version = ${version}`);
  return raw;
}

/** Starts the current app on top of `raw`: a fresh module registry, so `getDb()` opens and migrates again. */
function openWithCurrentApp(raw: DatabaseSync = new DatabaseSync(":memory:")): SQLiteDatabase {
  let db: SQLiteDatabase | undefined;
  jest.isolateModules(() => {
    const sqlite: typeof import("./support/memorySqlite") = require("expo-sqlite");
    sqlite.placeDatabase("unieats.db", raw);
    const database: typeof import("../db/database") = require("../db/database");
    db = database.getDb();
  });
  return db!;
}

const userVersion = (db: SQLiteDatabase) => db.getFirstSync<{ user_version: number }>("PRAGMA user_version")?.user_version;
const columns = (db: SQLiteDatabase, table: string) =>
  db.getAllSync<{ name: string }>(`PRAGMA table_info(${table})`).map((c) => c.name);
const queuedOps = (db: SQLiteDatabase) =>
  db
    .getAllSync<{ opId: string; payload: string; userId: string | null }>("SELECT opId, payload, userId FROM outbox ORDER BY seq")
    .map((op) => ({ ...op, payload: JSON.parse(op.payload) }));
const spotUpdatedAt = (db: SQLiteDatabase, id: string) =>
  db.getFirstSync<{ updatedAt: number }>("SELECT updatedAt FROM spots WHERE id = ?", [id])?.updatedAt;

function insertV1Spot(raw: DatabaseSync, id: string, updatedAt: number, serverUpdatedAt: number, pendingSync: number) {
  raw
    .prepare(
      `INSERT INTO spots (id, name, category, rating, priceLevel, lat, lng, openNow, photoUrl, description,
         updatedAt, serverUpdatedAt, pendingSync) VALUES (?, ?, 'cafe', 4, 1, 0, 0, 1, '', 'd', ?, ?, ?)`,
    )
    .run(id, id, updatedAt, serverUpdatedAt, pendingSync);
}

describe("database migrations", () => {
  it("creates schema v4 on a fresh install", () => {
    const db = openWithCurrentApp();
    expect(userVersion(db)).toBe(MIGRATIONS.length);
    expect(MIGRATIONS).toHaveLength(4);
    expect(columns(db, "spots")).not.toContain("serverUpdatedAt");
    expect(columns(db, "outbox")).toContain("userId");
    expect(columns(db, "settings")).toEqual(["key", "value"]);
  });

  it("does nothing when an up-to-date database is opened again", () => {
    const raw = databaseAt(4);
    raw.exec("INSERT INTO settings (key, value) VALUES ('crashReportingConsent', 'true')");
    const db = openWithCurrentApp(raw);
    expect(userVersion(db)).toBe(4);
    expect(db.getFirstSync<{ value: string }>("SELECT value FROM settings")?.value).toBe("true");
  });

  it("v1 (l07) -> v4: a queued edit keeps its base version, pending rows carry it, ops have no owner", () => {
    const raw = databaseAt(1);
    insertV1Spot(raw, "spot-1", 5_000, 1_000, 1);
    insertV1Spot(raw, "spot-2", 2_000, 2_000, 0);
    raw.exec(`INSERT INTO outbox (opId, type, entityId, payload, createdAt) VALUES
      ('op-a', 'update', 'spot-1', '{"name":"Edited","description":"d","openNow":true,"editedAt":5000}', 5000),
      ('op-b', 'update', 'spot-gone', '{"name":"X","description":"d","openNow":true,"editedAt":6000}', 6000)`);

    const db = openWithCurrentApp(raw);

    expect(userVersion(db)).toBe(4);
    expect(queuedOps(db)).toEqual([
      {
        opId: "op-a",
        userId: null,
        payload: { name: "Edited", description: "d", openNow: true, editedAt: 5000, updatedAt: 1000 },
      },
      { opId: "op-b", userId: null, payload: { name: "X", description: "d", openNow: true, editedAt: 6000, updatedAt: 0 } },
    ]);
    expect(spotUpdatedAt(db, "spot-1")).toBe(1_000);
    expect(spotUpdatedAt(db, "spot-2")).toBe(2_000);
    expect(columns(db, "spots")).not.toContain("serverUpdatedAt");
    expect(columns(db, "settings")).toEqual(["key", "value"]);
  });

  it("v2 (l08) -> v4 keeps the user id of each queued op", () => {
    const raw = databaseAt(2);
    insertV1Spot(raw, "spot-1", 3_000, 2_500, 1);
    raw.exec(`INSERT INTO outbox (opId, type, entityId, payload, createdAt, userId)
      VALUES ('op-c', 'update', 'spot-1', '{"name":"Y","description":"d","openNow":false}', 1, 'user-1')`);

    const db = openWithCurrentApp(raw);

    expect(queuedOps(db)).toEqual([
      { opId: "op-c", userId: "user-1", payload: { name: "Y", description: "d", openNow: false, updatedAt: 2500 } },
    ]);
    expect(spotUpdatedAt(db, "spot-1")).toBe(2_500);
  });

  it("v3 (l10) -> v4 only adds the settings table", () => {
    const raw = databaseAt(3);
    raw.exec(`INSERT INTO spots (id, name, category, rating, priceLevel, lat, lng, openNow, photoUrl, description,
      updatedAt, pendingSync) VALUES ('spot-1', 'Soup', 'soup', 4.5, 1, 0, 0, 1, '', 'd', 400, 1)`);
    raw.exec(`INSERT INTO outbox (opId, type, entityId, payload, createdAt, userId)
      VALUES ('op-d', 'update', 'spot-1', '{"name":"Z","description":"d","openNow":true,"updatedAt":400}', 1, 'user-2')`);

    const db = openWithCurrentApp(raw);

    expect(userVersion(db)).toBe(4);
    expect(queuedOps(db)).toEqual([
      { opId: "op-d", userId: "user-2", payload: { name: "Z", description: "d", openNow: true, updatedAt: 400 } },
    ]);
    expect(spotUpdatedAt(db, "spot-1")).toBe(400);
    expect(db.getFirstSync<{ n: number }>("SELECT COUNT(*) AS n FROM settings")?.n).toBe(0);
  });
});
