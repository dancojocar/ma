import { memo } from "react";
import { Image, Pressable, StyleSheet, Text, View, type AccessibilityActionEvent } from "react-native";
import type { LocalSpot } from "../domain/models";
import { FavouriteButton } from "./FavouriteButton";
import { priceLabel } from "./format";

type SpotCardProps = {
  spot: LocalSpot;
  isFavourite: boolean;
  onPress: (id: string) => void;
  onToggleFavourite: (id: string) => void;
  onHide: (id: string) => void;
};

const HIDE_ACTION = [{ name: "hide", label: "Hide" }];

export const SpotCard = memo(function SpotCard({
  spot,
  isFavourite,
  onPress,
  onToggleFavourite,
  onHide,
}: SpotCardProps) {
  const label = `${spot.name}, ${spot.category}, ${spot.openNow ? "open" : "closed"}, rated ${spot.rating.toFixed(1)} out of 5, ${priceLabel(spot.priceLevel)}${spot.pendingSync ? ", pending sync" : ""}`;
  return (
    <View style={styles.card}>
      <Pressable
        testID="spot-list-item"
        accessibilityRole="button"
        accessibilityLabel={label}
        accessibilityHint="Opens details"
        style={({ pressed }) => [styles.body, pressed && styles.pressed]}
        onPress={() => onPress(spot.id)}
        accessibilityActions={HIDE_ACTION}
        onAccessibilityAction={(e: AccessibilityActionEvent) => {
          if (e.nativeEvent.actionName === "hide") onHide(spot.id);
        }}
      >
        {spot.photoUrl ? (
          <Image source={{ uri: spot.photoUrl }} style={styles.thumb} accessible={false} />
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
          <Text style={styles.rating}>★ {spot.rating.toFixed(1)}</Text>
          <Text style={styles.price}>{priceLabel(spot.priceLevel)}</Text>
        </View>
      </Pressable>
      <FavouriteButton isFavourite={isFavourite} onToggle={() => onToggleFavourite(spot.id)} />
    </View>
  );
}, sameProps);

// Every change to the spots table re-reads all rows as new objects, so compare the spot by value.
function sameProps(prev: SpotCardProps, next: SpotCardProps): boolean {
  const keys = Object.keys(next.spot) as (keyof LocalSpot)[];
  return (
    prev.isFavourite === next.isFavourite &&
    prev.onPress === next.onPress &&
    prev.onToggleFavourite === next.onToggleFavourite &&
    prev.onHide === next.onHide &&
    keys.every((k) => prev.spot[k] === next.spot[k])
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
  body: { flex: 1, flexDirection: "row", alignItems: "center", gap: 12 },
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
  rating: { fontSize: 14, color: "#f0a500", fontWeight: "600" },
  price: { fontSize: 13, color: "#777" },
});
