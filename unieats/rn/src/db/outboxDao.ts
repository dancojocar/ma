import type { OutboxOp } from "../domain/models";
import { notifyChanged } from "./changes";
import { getDb } from "./database";

type OutboxRow = Omit<OutboxOp, "payload"> & { payload: string };

export function enqueue(op: Omit<OutboxOp, "seq">) {
  getDb().runSync(
    "INSERT INTO outbox (opId, type, entityId, payload, createdAt) VALUES (?, ?, ?, ?, ?)",
    [op.opId, op.type, op.entityId, JSON.stringify(op.payload), op.createdAt],
  );
}

export function allInOrder(): OutboxOp[] {
  return getDb()
    .getAllSync<OutboxRow>("SELECT * FROM outbox ORDER BY seq")
    .map((row) => ({ ...row, payload: JSON.parse(row.payload) as OutboxOp["payload"] }));
}

export function remove(seq: number) {
  getDb().runSync("DELETE FROM outbox WHERE seq = ?", [seq]);
  notifyChanged("outbox");
}

export function hasOpsFor(entityId: string): boolean {
  return getDb().getFirstSync("SELECT 1 FROM outbox WHERE entityId = ? LIMIT 1", [entityId]) !== null;
}

export function count(): number {
  return getDb().getFirstSync<{ n: number }>("SELECT COUNT(*) AS n FROM outbox")?.n ?? 0;
}
