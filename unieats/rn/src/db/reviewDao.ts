import type { Review } from "../domain/models";
import { notifyChanged } from "./changes";
import { getDb } from "./database";

export function upsertReviews(reviews: Review[]) {
  const db = getDb();
  db.withTransactionSync(() => {
    for (const r of reviews) {
      db.runSync(
        "INSERT OR REPLACE INTO reviews (id, spotId, author, stars, text, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
        [r.id, r.spotId, r.author, r.stars, r.text, r.createdAt],
      );
    }
  });
  notifyChanged("reviews");
}

export function reviewsForSpot(spotId: string): Review[] {
  return getDb().getAllSync<Review>(
    "SELECT * FROM reviews WHERE spotId = ? ORDER BY createdAt DESC",
    [spotId],
  );
}
