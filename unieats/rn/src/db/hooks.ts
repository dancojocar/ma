import { useMemo } from "react";
import type { CategoryFilter } from "../store/spotsStore";
import { useTableVersion } from "./changes";
import * as outboxDao from "./outboxDao";
import { reviewsForSpot } from "./reviewDao";
import { getSpotById, querySpots } from "./spotDao";

// `version` is deliberately a memo dependency: it changes whenever a DAO writes that table.
export function useLocalSpots(q: string, category: CategoryFilter) {
  const version = useTableVersion("spots");
  return useMemo(() => querySpots(q, category), [version, q, category]);
}

export function useLocalSpot(id: string) {
  const version = useTableVersion("spots");
  return useMemo(() => getSpotById(id), [version, id]);
}

export function useLocalReviews(spotId: string) {
  const version = useTableVersion("reviews");
  return useMemo(() => reviewsForSpot(spotId), [version, spotId]);
}

export function usePendingChangesCount() {
  const version = useTableVersion("outbox");
  return useMemo(() => outboxDao.count(), [version]);
}
