import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import type { Review } from "@unieats/shared";
import { detailStyles } from "./SpotDetail";
import { starsLabel } from "./format";

export function ReviewList({
  reviews,
  isLoading,
  error,
  onRetry,
}: {
  reviews: Review[] | undefined;
  isLoading: boolean;
  error: string | null;
  onRetry: () => void;
}) {
  return (
    <View style={detailStyles.section} testID="spot-detail-reviews">
      <Text style={detailStyles.sectionTitle}>Reviews ({reviews?.length ?? 0})</Text>
      {isLoading && <ActivityIndicator color="#e87c2a" />}
      {error && (
        <Pressable onPress={onRetry} accessibilityRole="button">
          <Text style={styles.error}>{error} Tap to retry.</Text>
        </Pressable>
      )}
      {reviews?.map((r) => (
        <View key={r.id} style={styles.review}>
          <View style={styles.header}>
            <Text style={styles.author}>{r.author}</Text>
            <Text style={styles.date}>{new Date(r.createdAt).toLocaleDateString()}</Text>
          </View>
          <Text style={styles.stars}>{starsLabel(r.stars)}</Text>
          <Text style={styles.text}>{r.text}</Text>
        </View>
      ))}
      {reviews?.length === 0 && <Text style={styles.empty}>No reviews yet.</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  review: { borderTopWidth: 1, borderTopColor: "#f0f0f0", paddingTop: 10 },
  header: { flexDirection: "row", justifyContent: "space-between" },
  author: { fontWeight: "600", color: "#333" },
  date: { color: "#999", fontSize: 12 },
  stars: { color: "#f0a500" },
  text: { color: "#555", fontSize: 14, lineHeight: 20 },
  empty: { color: "#999", fontSize: 14 },
  error: { color: "#c0392b", fontSize: 14 },
});
