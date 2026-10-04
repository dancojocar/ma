import { useState } from "react";
import { FlatList, Pressable, StyleSheet, Text, View } from "react-native";
import { SpotCard } from "../../../src/components/SpotCard";
import { SpotDetail } from "../../../src/components/SpotDetail";
import { SEED_REVIEWS, SEED_SPOTS } from "../../../src/data/seeds";

export default function SpotListScreen() {
  const [selectedSpotId, setSelectedSpotId] = useState<string | null>(null);
  const selected = SEED_SPOTS.find((s) => s.id === selectedSpotId);

  if (selected) {
    return (
      <View style={styles.container}>
        <Pressable onPress={() => setSelectedSpotId(null)} style={styles.back}>
          <Text style={styles.backText}>← Back to list</Text>
        </Pressable>
        <SpotDetail spot={selected} reviews={SEED_REVIEWS[selected.id] ?? []} />
      </View>
    );
  }

  return (
    <FlatList
      style={styles.container}
      data={SEED_SPOTS}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => (
        <SpotCard spot={item} onPress={() => setSelectedSpotId(item.id)} />
      )}
      ItemSeparatorComponent={Separator}
      contentContainerStyle={styles.list}
    />
  );
}

function Separator() {
  return <View style={styles.separator} />;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  list: { padding: 12 },
  separator: { height: 10 },
  back: { paddingHorizontal: 16, paddingTop: 12 },
  backText: { color: "#e87c2a", fontSize: 16, fontWeight: "600" },
});
