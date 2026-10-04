import type { LocalSpot, Spot, SpotEdit } from "../domain/models";
import type { CategoryFilter } from "../store/spotsStore";
import { notifyChanged } from "./changes";
import { getDb } from "./database";

type SpotRow = Omit<LocalSpot, "openNow" | "pendingSync"> & { openNow: number; pendingSync: number };

const toLocalSpot = (row: SpotRow): LocalSpot => ({
  ...row,
  openNow: row.openNow === 1,
  pendingSync: row.pendingSync === 1,
});

const spotParams = (s: Spot) => [
  s.id, s.name, s.category, s.rating, s.priceLevel, s.lat, s.lng,
  s.openNow ? 1 : 0, s.photoUrl, s.description, s.updatedAt,
];

const COLUMNS =
  "id, name, category, rating, priceLevel, lat, lng, openNow, photoUrl, description, updatedAt, pendingSync";

// Upsert, never delete-then-insert, and leave rows with unsynced local edits alone.
const UPSERT_FROM_SERVER = `
  INSERT INTO spots (${COLUMNS}) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
  ON CONFLICT(id) DO UPDATE SET
    name = excluded.name, category = excluded.category, rating = excluded.rating,
    priceLevel = excluded.priceLevel, lat = excluded.lat, lng = excluded.lng,
    openNow = excluded.openNow, photoUrl = excluded.photoUrl,
    description = excluded.description, updatedAt = excluded.updatedAt
  WHERE spots.pendingSync = 0`;

export function upsertFromServer(spots: Spot[]) {
  const db = getDb();
  db.withTransactionSync(() => {
    for (const spot of spots) db.runSync(UPSERT_FROM_SERVER, spotParams(spot));
  });
  notifyChanged("spots");
}

export function querySpots(q: string, category: CategoryFilter): LocalSpot[] {
  const where: string[] = [];
  const args: string[] = [];
  if (q) {
    where.push("(name LIKE ? OR description LIKE ?)");
    args.push(`%${q}%`, `%${q}%`);
  }
  if (category !== "all") {
    where.push("category = ?");
    args.push(category);
  }
  const sql = `SELECT * FROM spots ${where.length ? `WHERE ${where.join(" AND ")}` : ""} ORDER BY name COLLATE NOCASE`;
  return getDb().getAllSync<SpotRow>(sql, args).map(toLocalSpot);
}

export function getSpotById(id: string): LocalSpot | null {
  const row = getDb().getFirstSync<SpotRow>("SELECT * FROM spots WHERE id = ?", [id]);
  return row ? toLocalSpot(row) : null;
}

export function getPendingSync(): LocalSpot[] {
  return getDb().getAllSync<SpotRow>("SELECT * FROM spots WHERE pendingSync = 1").map(toLocalSpot);
}

export function recordServerVersion(id: string, updatedAt: number) {
  getDb().runSync("UPDATE spots SET updatedAt = ? WHERE id = ?", [updatedAt, id]);
}

/** Keeps `updatedAt`: a pending row carries the server version its edit is based on. */
export function applyLocalEdit(id: string, edit: SpotEdit) {
  getDb().runSync(
    "UPDATE spots SET name = ?, description = ?, openNow = ?, pendingSync = 1 WHERE id = ?",
    [edit.name, edit.description, edit.openNow ? 1 : 0, id],
  );
}

/** Replace the row with the server copy and clear the pending flag. */
export function markSynced(spot: Spot) {
  getDb().runSync(
    `INSERT OR REPLACE INTO spots (${COLUMNS}) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)`,
    spotParams(spot),
  );
  notifyChanged("spots");
}

export function deleteIfNotPending(id: string) {
  getDb().runSync("DELETE FROM spots WHERE id = ? AND pendingSync = 0", [id]);
  notifyChanged("spots");
}

export function deleteSpot(id: string) {
  getDb().runSync("DELETE FROM spots WHERE id = ?", [id]);
  notifyChanged("spots");
}
