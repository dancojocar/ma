import { useRouter } from "expo-router";
import { useCallback, useMemo, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from "react-native";
import { describeError } from "../../../src/api/errors";
import { CategoryChips } from "../../../src/components/CategoryChips";
import { SearchBar } from "../../../src/components/SearchBar";
import { SpotCard } from "../../../src/components/SpotCard";
import { EmptyView, ErrorView, LoadingView } from "../../../src/components/StatusViews";
import { useDebouncedValue } from "../../../src/hooks/useDebouncedValue";
import { useSpotsInfinite } from "../../../src/hooks/useSpots";
import { useSpotsStore } from "../../../src/store/spotsStore";

export default function SpotListScreen() {
  const router = useRouter();
  const searchQuery = useSpotsStore((s) => s.searchQuery);
  const categoryFilter = useSpotsStore((s) => s.categoryFilter);
  const favouriteIds = useSpotsStore((s) => s.favouriteIds);
  const setSearchQuery = useSpotsStore((s) => s.setSearchQuery);
  const setCategoryFilter = useSpotsStore((s) => s.setCategoryFilter);
  const toggleFavourite = useSpotsStore((s) => s.toggleFavourite);

  const q = useDebouncedValue(searchQuery.trim(), 300);
  const query = useSpotsInfinite(q, categoryFilter);
  const spots = useMemo(() => query.data?.pages.flatMap((p) => p.spots) ?? [], [query.data]);

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await query.refetch();
    setRefreshing(false);
  }, [query]);

  const onEndReached = () => {
    if (query.hasNextPage && !query.isFetchingNextPage && !query.isFetchNextPageError) {
      query.fetchNextPage();
    }
  };

  let body;
  if (query.isPending) {
    body = <LoadingView />;
  } else if (query.isError && spots.length === 0) {
    body = <ErrorView message={describeError(query.error)} onRetry={() => query.refetch()} />;
  } else {
    body = (
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
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
        onEndReached={onEndReached}
        onEndReachedThreshold={0.5}
        ListEmptyComponent={<EmptyView message="No spots match your search." />}
        ListFooterComponent={
          query.isFetchingNextPage ? (
            <ActivityIndicator style={styles.footer} color="#e87c2a" />
          ) : query.isFetchNextPageError ? (
            <Pressable style={styles.footer} onPress={() => query.fetchNextPage()}>
              <Text style={styles.footerError}>Couldn't load more. Tap to retry.</Text>
            </Pressable>
          ) : null
        }
      />
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.filters}>
        <SearchBar query={searchQuery} onQueryChange={setSearchQuery} />
        <CategoryChips value={categoryFilter} onChange={setCategoryFilter} />
      </View>
      {body}
    </View>
  );
}

function Separator() {
  return <View style={styles.separator} />;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  filters: { padding: 12, gap: 10, backgroundColor: "#fff" },
  list: { padding: 12, flexGrow: 1 },
  separator: { height: 10 },
  footer: { padding: 16, alignItems: "center" },
  footerError: { color: "#c0392b" },
});
