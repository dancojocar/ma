import { Pressable, StyleSheet, Text } from "react-native";

export function FavouriteButton({
  isFavourite,
  onToggle,
  size = 20,
}: {
  isFavourite: boolean;
  onToggle: () => void;
  size?: number;
}) {
  return (
    <Pressable
      onPress={onToggle}
      hitSlop={8}
      accessibilityRole="button"
      accessibilityLabel={isFavourite ? "Remove from favourites" : "Add to favourites"}
    >
      <Text style={[styles.heart, { fontSize: size }, isFavourite && styles.active]}>
        {isFavourite ? "♥" : "♡"}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  heart: { color: "#bbb" },
  active: { color: "#e87c2a" },
});
