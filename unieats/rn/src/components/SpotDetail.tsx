import { ScrollView, StyleSheet, Text, View } from "react-native";
import type { Review, Spot } from "../domain/models";
import { FavouriteButton } from "./FavouriteButton";
import { priceLabel, starsLabel } from "./format";

export function SpotDetail({
  spot,
  reviews,
  isFavourite,
  onToggleFavourite,
}: {
  spot: Spot;
  reviews: Review[];
  isFavourite: boolean;
  onToggleFavourite: () => void;
}) {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.section}>
        <View style={styles.header}>
          <Text style={styles.name}>{spot.name}</Text>
          <FavouriteButton isFavourite={isFavourite} onToggle={onToggleFavourite} size={28} />
        </View>
        <View style={styles.row}>
          <Text style={styles.badge}>{spot.category}</Text>
          <Text style={[styles.badge, spot.openNow ? styles.open : styles.closed]}>
            {spot.openNow ? "Open" : "Closed"}
          </Text>
        </View>
        <View style={styles.row}>
          <Text style={styles.rating}>★ {spot.rating.toFixed(1)}</Text>
          <Text style={styles.price}>{priceLabel(spot.priceLevel)}</Text>
        </View>
        <Text style={styles.description}>{spot.description}</Text>
      </View>

      <View style={styles.section} testID="spot-detail-reviews">
        <Text style={styles.sectionTitle}>Reviews ({reviews.length})</Text>
        {reviews.map((r) => (
          <View key={r.id} style={styles.review}>
            <View style={styles.reviewHeader}>
              <Text style={styles.author}>{r.author}</Text>
              <Text style={styles.date}>{new Date(r.createdAt).toLocaleDateString()}</Text>
            </View>
            <Text style={styles.rating}>{starsLabel(r.stars)}</Text>
            <Text style={styles.reviewText}>{r.text}</Text>
          </View>
        ))}
        {reviews.length === 0 && <Text style={styles.empty}>No reviews yet.</Text>}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  content: { padding: 12, gap: 10, paddingBottom: 40 },
  section: { backgroundColor: "#fff", borderRadius: 12, padding: 16, gap: 8 },
  header: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  name: { flex: 1, fontSize: 22, fontWeight: "700", color: "#222" },
  row: { flexDirection: "row", gap: 12, alignItems: "center" },
  badge: {
    backgroundColor: "#f0e8df",
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 3,
    fontSize: 12,
    color: "#555",
    textTransform: "capitalize",
    overflow: "hidden",
  },
  open: { backgroundColor: "#e6f4ea" },
  closed: { backgroundColor: "#fce8e6" },
  rating: { fontSize: 14, color: "#f0a500", fontWeight: "600" },
  price: { fontSize: 13, color: "#777" },
  description: { fontSize: 14, color: "#444", lineHeight: 20 },
  sectionTitle: { fontSize: 18, fontWeight: "700", color: "#222" },
  review: { borderTopWidth: 1, borderTopColor: "#f0f0f0", paddingTop: 10 },
  reviewHeader: { flexDirection: "row", justifyContent: "space-between" },
  author: { fontWeight: "600", color: "#333" },
  date: { color: "#999", fontSize: 12 },
  reviewText: { color: "#555", fontSize: 14, lineHeight: 20 },
  empty: { color: "#999", fontSize: 14 },
});
