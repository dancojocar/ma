import { useMutation } from "@tanstack/react-query";
import { Stack, useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { Pressable, StyleSheet, Text } from "react-native";
import { describeError } from "../../../src/api/errors";
import { AddReviewForm } from "../../../src/components/AddReviewForm";
import { EditSpotForm } from "../../../src/components/EditSpotForm";
import { ReviewList } from "../../../src/components/ReviewList";
import { SpotDetail } from "../../../src/components/SpotDetail";
import { EmptyView, ErrorView, LoadingView } from "../../../src/components/StatusViews";
import { SyncBanner } from "../../../src/components/SyncBanner";
import { useLocalReviews, useLocalSpot } from "../../../src/db/hooks";
import { useReviewsSync, useSpotSync } from "../../../src/hooks/useSpots";
import { addReview, editSpot } from "../../../src/repository/spotRepository";
import { useSpotsStore } from "../../../src/store/spotsStore";

export default function SpotDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const isFavourite = useSpotsStore((s) => s.favouriteIds.has(id));
  const toggleFavourite = useSpotsStore((s) => s.toggleFavourite);
  const spot = useLocalSpot(id);
  const reviews = useLocalReviews(id);
  const spotSync = useSpotSync(id);
  const reviewsSync = useReviewsSync(id);
  const [editing, setEditing] = useState(false);
  const postReview = useMutation({
    mutationFn: (review: { stars: number; text: string }) => addReview(id, review),
  });

  if (!spot) {
    if (spotSync.isPending) return <LoadingView />;
    if (spotSync.isError) {
      return <ErrorView message={describeError(spotSync.error)} onRetry={() => spotSync.refetch()} />;
    }
    return <EmptyView message="This spot no longer exists." />;
  }

  return (
    <>
      <Stack.Screen options={{ title: spot.name }} />
      <SyncBanner />
      <SpotDetail
        spot={spot}
        isFavourite={isFavourite}
        onToggleFavourite={() => toggleFavourite(spot.id)}
      >
        {editing ? (
          <EditSpotForm
            initial={spot}
            onCancel={() => setEditing(false)}
            onSave={(edit) => {
              editSpot(spot.id, edit);
              setEditing(false);
            }}
          />
        ) : (
          <Pressable style={styles.editButton} onPress={() => setEditing(true)} testID="edit-spot-button">
            <Text style={styles.editText}>Edit spot</Text>
          </Pressable>
        )}
        <ReviewList
          reviews={reviewsSync.isPending && reviews.length === 0 ? undefined : reviews}
          isLoading={reviewsSync.isPending && reviews.length === 0}
          error={reviewsSync.isError && reviews.length === 0 ? describeError(reviewsSync.error) : null}
          onRetry={() => reviewsSync.refetch()}
        />
        <AddReviewForm
          submitting={postReview.isPending}
          error={postReview.isError ? describeError(postReview.error) : null}
          onSubmit={(review, reset) => postReview.mutate(review, { onSuccess: reset })}
        />
      </SpotDetail>
    </>
  );
}

const styles = StyleSheet.create({
  editButton: {
    borderWidth: 1,
    borderColor: "#e87c2a",
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: "center",
    backgroundColor: "#fff",
  },
  editText: { color: "#e87c2a", fontWeight: "600", fontSize: 15 },
});
