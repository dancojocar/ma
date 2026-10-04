import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import type { LocalSpot } from "@unieats/shared";
import { FavouriteButton } from "./FavouriteButton";
import { priceLabel } from "./format";
import { RatingBadge } from "./RatingBadge";

export function SpotCard({
  spot,
  isFavourite,
  onPress,
  onToggleFavourite,
}: {
  spot: LocalSpot;
  isFavourite: boolean;
  onPress: () => void;
  onToggleFavourite: () => void;
}) {
  return (
    <Pressable
      testID="spot-list-item"
      style={({ pressed }) => [styles.card, pressed && styles.pressed]}
      onPress={onPress}
    >
      {spot.photoUrl ? (
        <Image source={{ uri: spot.photoUrl }} style={styles.thumb} />
      ) : (
        <View style={[styles.thumb, styles.thumbPlaceholder]} />
      )}
      <View style={styles.main}>
        <Text style={styles.name} numberOfLines={1}>
          {spot.name}
        </Text>
        <View style={styles.badgeRow}>
          <Text style={styles.badge}>{spot.category}</Text>
          <Text style={[styles.badge, spot.openNow ? styles.open : styles.closed]}>
            {spot.openNow ? "Open" : "Closed"}
          </Text>
          {spot.pendingSync && <Text style={[styles.badge, styles.pending]}>Pending sync</Text>}
        </View>
      </View>
      <View style={styles.meta}>
        <RatingBadge rating={spot.rating} />
        <Text style={styles.price}>{priceLabel(spot.priceLevel)}</Text>
        <FavouriteButton isFavourite={isFavourite} onToggle={onToggleFavourite} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    backgroundColor: "#fff",
    borderRadius: 12,
    padding: 12,
    elevation: 2,
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
  },
  pressed: { opacity: 0.75 },
  thumb: { width: 56, height: 56, borderRadius: 8 },
  thumbPlaceholder: { backgroundColor: "#f0e8df" },
  main: { flex: 1 },
  name: { fontSize: 16, fontWeight: "600", color: "#222", marginBottom: 6 },
  badgeRow: { flexDirection: "row", gap: 6 },
  badge: {
    backgroundColor: "#f0e8df",
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 3,
    fontSize: 11,
    color: "#555",
    textTransform: "capitalize",
    overflow: "hidden",
  },
  open: { backgroundColor: "#e6f4ea" },
  closed: { backgroundColor: "#fce8e6" },
  pending: { backgroundColor: "#fff4e5", color: "#8a4b00", textTransform: "none" },
  meta: { alignItems: "flex-end", gap: 4 },
  price: { fontSize: 13, color: "#777" },
});
