import { Pressable, StyleSheet, Text, View } from "react-native";
import type { Spot } from "../domain/models";
import { FavouriteButton } from "./FavouriteButton";
import { priceLabel } from "./format";

export function SpotCard({
  spot,
  isFavourite,
  onPress,
  onToggleFavourite,
}: {
  spot: Spot;
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
      <View style={styles.main}>
        <Text style={styles.name} numberOfLines={1}>
          {spot.name}
        </Text>
        <View style={styles.badgeRow}>
          <Text style={styles.badge}>{spot.category}</Text>
          <Text style={[styles.badge, spot.openNow ? styles.open : styles.closed]}>
            {spot.openNow ? "Open" : "Closed"}
          </Text>
        </View>
      </View>
      <View style={styles.meta}>
        <Text style={styles.rating}>★ {spot.rating.toFixed(1)}</Text>
        <Text style={styles.price}>{priceLabel(spot.priceLevel)}</Text>
        <FavouriteButton isFavourite={isFavourite} onToggle={onToggleFavourite} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    backgroundColor: "#fff",
    borderRadius: 12,
    padding: 14,
    elevation: 2,
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
  },
  pressed: { opacity: 0.75 },
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
  meta: { alignItems: "flex-end", gap: 4 },
  rating: { fontSize: 14, color: "#f0a500", fontWeight: "600" },
  price: { fontSize: 13, color: "#777" },
});
