import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { getReviews, getSpot, listSpots } from "../api/client";
import type { CategoryFilter } from "../store/spotsStore";

export function useSpotsInfinite(q: string, category: CategoryFilter) {
  return useInfiniteQuery({
    queryKey: ["spots", q, category],
    queryFn: ({ pageParam, signal }) => listSpots({ page: pageParam, q, category, signal }),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => (lastPage.hasNextPage ? lastPage.page + 1 : undefined),
  });
}

export function useSpot(id: string) {
  return useQuery({
    queryKey: ["spot", id],
    queryFn: ({ signal }) => getSpot(id, signal),
  });
}

export function useReviews(spotId: string) {
  return useQuery({
    queryKey: ["reviews", spotId],
    queryFn: ({ signal }) => getReviews(spotId, signal),
  });
}
