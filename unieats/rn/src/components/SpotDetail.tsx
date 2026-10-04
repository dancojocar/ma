import type { ReactNode } from "react";
import { Image, ScrollView, StyleSheet, Text, View } from "react-native";
import type { LocalSpot } from "../domain/models";
import { FavouriteButton } from "./FavouriteButton";
import { priceLabel } from "./format";
import { RatingBadge } from "./RatingBadge";

export function SpotDetail({
  spot,
  isFavourite,
  onToggleFavourite,
  children,
}: {
  spot: LocalSpot;
  isFavourite: boolean;
  onToggleFavourite: () => void;
  children?: ReactNode;
}) {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {spot.photoUrl ? <Image source={{ uri: spot.photoUrl }} style={styles.photo} /> : null}
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
          {spot.pendingSync && <Text style={[styles.badge, styles.pending]}>Pending sync</Text>}
        </View>
        <View style={styles.row}>
          <RatingBadge rating={spot.rating} />
          <Text style={styles.price}>{priceLabel(spot.priceLevel)}</Text>
        </View>
        <Text style={styles.description}>{spot.description}</Text>
      </View>
      {children}
    </ScrollView>
  );
}

export const detailStyles = StyleSheet.create({
  section: { backgroundColor: "#fff", borderRadius: 12, padding: 16, gap: 8 },
  sectionTitle: { fontSize: 18, fontWeight: "700", color: "#222" },
});

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  content: { padding: 12, gap: 10, paddingBottom: 40 },
  photo: { width: "100%", height: 200, borderRadius: 12 },
  section: detailStyles.section,
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
  pending: { backgroundColor: "#fff4e5", color: "#8a4b00", textTransform: "none" },
  price: { fontSize: 13, color: "#777" },
  description: { fontSize: 14, color: "#444", lineHeight: 20 },
});
