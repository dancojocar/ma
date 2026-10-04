import { FlatList, Pressable, StyleSheet, Text } from "react-native";
import type { CategoryFilter } from "../store/spotsStore";

const CATEGORIES: { label: string; value: CategoryFilter }[] = [
  { label: "All", value: "all" },
  { label: "Cafe", value: "cafe" },
  { label: "Canteen", value: "canteen" },
  { label: "Fast food", value: "fastfood" },
  { label: "Bakery", value: "bakery" },
  { label: "Bar", value: "bar" },
];

export function CategoryChips({
  value,
  onChange,
}: {
  value: CategoryFilter;
  onChange: (value: CategoryFilter) => void;
}) {
  return (
    <FlatList
      horizontal
      showsHorizontalScrollIndicator={false}
      data={CATEGORIES}
      keyExtractor={(item) => item.value}
      contentContainerStyle={styles.list}
      renderItem={({ item }) => {
        const active = item.value === value;
        return (
          <Pressable
            style={[styles.chip, active && styles.chipActive]}
            onPress={() => onChange(item.value)}
          >
            <Text style={[styles.text, active && styles.textActive]}>{item.label}</Text>
          </Pressable>
        );
      }}
    />
  );
}

const styles = StyleSheet.create({
  list: { gap: 8 },
  chip: {
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 6,
    backgroundColor: "#fafafa",
  },
  chipActive: { backgroundColor: "#e87c2a", borderColor: "#e87c2a" },
  text: { fontSize: 13, color: "#555" },
  textActive: { color: "#fff", fontWeight: "600" },
});
