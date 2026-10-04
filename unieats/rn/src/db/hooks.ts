import { useCallback, useRef, useSyncExternalStore } from "react";
import type { CategoryFilter } from "../store/spotsStore";
import { subscribeTables, tableVersion, type Table } from "./changes";
import * as outboxDao from "./outboxDao";
import { reviewsForSpot } from "./reviewDao";
import { getSpotById, querySpots } from "./spotDao";

type Snapshot<T> = { version: number; key: string; value: T };

// getSnapshot must return the same reference until the table changes, so the SQL result is cached
// per (table version, key). `key` must encode every argument `read` closes over.
function useTableQuery<T>(table: Table, key: string, read: () => T): T {
  const cache = useRef<Snapshot<T> | null>(null);
  const getSnapshot = useCallback(() => {
    const version = tableVersion(table);
    const cached = cache.current;
    if (cached && cached.version === version && cached.key === key) return cached.value;
    const value = read();
    cache.current = { version, key, value };
    return value;
  }, [table, key]);
  return useSyncExternalStore(subscribeTables, getSnapshot);
}

export function useLocalSpots(q: string, category: CategoryFilter) {
  return useTableQuery("spots", `${q}\0${category}`, () => querySpots(q, category));
}

export function useLocalSpot(id: string) {
  return useTableQuery("spots", id, () => getSpotById(id));
}

export function useLocalReviews(spotId: string) {
  return useTableQuery("reviews", spotId, () => reviewsForSpot(spotId));
}

export function usePendingChangesCount() {
  return useTableQuery("outbox", "", outboxDao.count);
}
