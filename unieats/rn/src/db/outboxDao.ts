import type { OutboxOp } from "../domain/models";
import { notifyChanged } from "./changes";
import { getDb } from "./database";

type OutboxRow = Omit<OutboxOp, "payload"> & { payload: string };

export function enqueue(op: Omit<OutboxOp, "seq">) {
  getDb().runSync(
    "INSERT INTO outbox (opId, type, entityId, userId, payload, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
    [op.opId, op.type, op.entityId, op.userId, JSON.stringify(op.payload), op.createdAt],
  );
}

export function oldest(): OutboxOp | null {
  const row = getDb().getFirstSync<OutboxRow>("SELECT * FROM outbox ORDER BY seq LIMIT 1");
  return row ? { ...row, payload: JSON.parse(row.payload) as OutboxOp["payload"] } : null;
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

export function rebase(entityId: string, updatedAt: number) {
  getDb().runSync(
    "UPDATE outbox SET payload = json_set(payload, '$.updatedAt', ?) WHERE entityId = ?",
    [updatedAt, entityId],
  );
}

/** Edits based on a version older than the server's would be rejected with 409 as well. */
export function removeStale(entityId: string, serverUpdatedAt: number) {
  getDb().runSync(
    "DELETE FROM outbox WHERE entityId = ? AND json_extract(payload, '$.updatedAt') < ?",
    [entityId, serverUpdatedAt],
  );
  notifyChanged("outbox");
}

export function clear(): number {
  return getDb().runSync("DELETE FROM outbox").changes;
}

export function removeOtherUsersOps(userId: string): number {
  return getDb().runSync("DELETE FROM outbox WHERE userId IS NOT NULL AND userId <> ?", [userId]).changes;
}
