import { useRouter } from "expo-router";
import { FlatList, StyleSheet, Text, View } from "react-native";
import { CategoryChips } from "../../../src/components/CategoryChips";
import { SearchBar } from "../../../src/components/SearchBar";
import { SpotCard } from "../../../src/components/SpotCard";
import { SEED_SPOTS } from "../../../src/data/seeds";
import { filterSpots, useSpotsStore } from "../../../src/store/spotsStore";

export default function SpotListScreen() {
  const router = useRouter();
  const searchQuery = useSpotsStore((s) => s.searchQuery);
  const categoryFilter = useSpotsStore((s) => s.categoryFilter);
  const favouriteIds = useSpotsStore((s) => s.favouriteIds);
  const setSearchQuery = useSpotsStore((s) => s.setSearchQuery);
  const setCategoryFilter = useSpotsStore((s) => s.setCategoryFilter);
  const toggleFavourite = useSpotsStore((s) => s.toggleFavourite);

  const spots = filterSpots(SEED_SPOTS, searchQuery, categoryFilter);

  return (
    <View style={styles.container}>
      <View style={styles.filters}>
        <SearchBar query={searchQuery} onQueryChange={setSearchQuery} />
        <CategoryChips value={categoryFilter} onChange={setCategoryFilter} />
      </View>
      <FlatList
        data={spots}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <SpotCard
            spot={item}
            isFavourite={favouriteIds.has(item.id)}
            onPress={() => router.push({ pathname: "/spots/[id]", params: { id: item.id } })}
            onToggleFavourite={() => toggleFavourite(item.id)}
          />
        )}
        ItemSeparatorComponent={Separator}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Text style={styles.empty}>No spots match your search.</Text>}
      />
    </View>
  );
}

function Separator() {
  return <View style={styles.separator} />;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  filters: { padding: 12, gap: 10, backgroundColor: "#fff" },
  list: { padding: 12 },
  separator: { height: 10 },
  empty: { textAlign: "center", color: "#999", marginTop: 40 },
});
