import { Stack, useLocalSearchParams } from "expo-router";
import { describeError } from "../../../src/api/errors";
import { ReviewList } from "../../../src/components/ReviewList";
import { SpotDetail } from "../../../src/components/SpotDetail";
import { ErrorView, LoadingView } from "../../../src/components/StatusViews";
import { useReviews, useSpot } from "../../../src/hooks/useSpots";
import { useSpotsStore } from "../../../src/store/spotsStore";

export default function SpotDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const isFavourite = useSpotsStore((s) => s.favouriteIds.has(id));
  const toggleFavourite = useSpotsStore((s) => s.toggleFavourite);
  const spotQuery = useSpot(id);
  const reviewsQuery = useReviews(id);

  if (spotQuery.isPending) return <LoadingView />;
  if (spotQuery.isError) {
    return <ErrorView message={describeError(spotQuery.error)} onRetry={() => spotQuery.refetch()} />;
  }

  const spot = spotQuery.data;
  return (
    <>
      <Stack.Screen options={{ title: spot.name }} />
      <SpotDetail
        spot={spot}
        isFavourite={isFavourite}
        onToggleFavourite={() => toggleFavourite(spot.id)}
      >
        <ReviewList
          reviews={reviewsQuery.data}
          isLoading={reviewsQuery.isPending}
          error={reviewsQuery.isError ? describeError(reviewsQuery.error) : null}
          onRetry={() => reviewsQuery.refetch()}
        />
      </SpotDetail>
    </>
  );
}
