import { Stack, useLocalSearchParams } from "expo-router";
import { StyleSheet, Text, View } from "react-native";
import { SpotDetail } from "../../../src/components/SpotDetail";
import { SEED_REVIEWS, SEED_SPOTS } from "../../../src/data/seeds";
import { useSpotsStore } from "../../../src/store/spotsStore";

export default function SpotDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const isFavourite = useSpotsStore((s) => s.favouriteIds.has(id));
  const toggleFavourite = useSpotsStore((s) => s.toggleFavourite);
  const spot = SEED_SPOTS.find((s) => s.id === id);

  if (!spot) {
    return (
      <View style={styles.center}>
        <Stack.Screen options={{ title: "Not found" }} />
        <Text style={styles.notFound}>No spot with id "{id}".</Text>
      </View>
    );
  }

  return (
    <>
      <Stack.Screen options={{ title: spot.name }} />
      <SpotDetail
        spot={spot}
        reviews={SEED_REVIEWS[spot.id] ?? []}
        isFavourite={isFavourite}
        onToggleFavourite={() => toggleFavourite(spot.id)}
      />
    </>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, justifyContent: "center", alignItems: "center", padding: 24 },
  notFound: { fontSize: 16, color: "#666" },
});
