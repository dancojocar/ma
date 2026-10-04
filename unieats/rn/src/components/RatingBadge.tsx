import { StyleSheet, Text, View } from "react-native";
import { useFlag } from "../cloud/featureFlags";

export function RatingBadge({ rating }: { rating: number }) {
  const newUi = useFlag("show_new_rating_ui");
  if (!newUi) return <Text style={styles.classic}>★ {rating.toFixed(1)}</Text>;
  return (
    <View style={styles.pill} testID="new-rating-badge">
      <Text style={styles.pillText}>{rating.toFixed(1)}</Text>
      <Text style={styles.pillStar}>★</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  classic: { fontSize: 14, color: "#f0a500", fontWeight: "600" },
  pill: {
    flexDirection: "row",
    alignItems: "center",
    gap: 2,
    backgroundColor: "#2e7d32",
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  pillText: { color: "#fff", fontWeight: "700", fontSize: 13 },
  pillStar: { color: "#ffe082", fontSize: 12 },
});
