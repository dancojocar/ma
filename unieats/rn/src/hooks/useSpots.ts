import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { refreshReviews, refreshSpot, refreshSpotsPage } from "../repository/spotRepository";
import type { CategoryFilter } from "../store/spotsStore";

/** Network → SQLite. Screens render the database (src/db/hooks.ts); these queries only drive fetching. */
export function useSpotsSync(q: string, category: CategoryFilter) {
  return useInfiniteQuery({
    queryKey: ["spots", q, category],
    queryFn: ({ pageParam, signal }) => refreshSpotsPage({ page: pageParam, q, category, signal }),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => (lastPage.hasNextPage ? lastPage.page + 1 : undefined),
  });
}

export function useSpotSync(id: string) {
  return useQuery({
    queryKey: ["spot", id],
    queryFn: ({ signal }) => refreshSpot(id, signal),
  });
}

export function useReviewsSync(spotId: string) {
  return useQuery({
    queryKey: ["reviews", spotId],
    queryFn: ({ signal }) => refreshReviews(spotId, signal),
  });
}
