import { useRouter } from "expo-router";
import { FlatList, Linking, Pressable, StyleSheet, Text, View } from "react-native";
import { LiveBadge } from "../../src/components/LiveBadge";
import { EmptyView, LoadingView } from "../../src/components/StatusViews";
import { NEARBY_RADIUS_KM } from "@unieats/shared";
import { useLiveSpots } from "../../src/hooks/useLiveSpots";
import { useNearbySpots } from "../../src/hooks/useNearbySpots";

export default function NearbyScreen() {
  const router = useRouter();
  const { permission, requestPermission, located, nearby } = useNearbySpots();
  const liveStatus = useLiveSpots();

  if (permission === "unknown" || permission === "denied") {
    return (
      <View style={styles.center}>
        <Text style={styles.title}>Spots near you</Text>
        <Text style={styles.text}>
          UniEats uses your location only while this screen is open, to list food spots within{" "}
          {NEARBY_RADIUS_KM} km.
        </Text>
        <Pressable style={styles.button} onPress={requestPermission} testID="location-allow">
          <Text style={styles.buttonText}>Allow location</Text>
        </Pressable>
      </View>
    );
  }

  if (permission === "blocked") {
    return (
      <View style={styles.center}>
        <Text style={styles.text}>Location access is off for UniEats. Turn it on in Settings.</Text>
        <Pressable style={styles.button} onPress={() => Linking.openSettings()}>
          <Text style={styles.buttonText}>Open Settings</Text>
        </Pressable>
      </View>
    );
  }

  if (!located) return <LoadingView />;

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerText}>Within {NEARBY_RADIUS_KM} km</Text>
        <LiveBadge status={liveStatus} />
      </View>
      <FlatList
        data={nearby}
        keyExtractor={(item) => item.spot.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <Pressable
            style={styles.card}
            onPress={() => router.push({ pathname: "/spots/[id]", params: { id: item.spot.id } })}
          >
            <Text style={styles.name}>{item.spot.name}</Text>
            <Text style={styles.distance}>{(item.distanceKm * 1000).toFixed(0)} m away</Text>
          </Pressable>
        )}
        ListEmptyComponent={<EmptyView message={`No saved spots within ${NEARBY_RADIUS_KM} km.`} />}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  center: { flex: 1, justifyContent: "center", alignItems: "center", padding: 24, gap: 14 },
  title: { fontSize: 20, fontWeight: "700", color: "#222" },
  text: { fontSize: 15, color: "#555", textAlign: "center" },
  button: { backgroundColor: "#e87c2a", borderRadius: 10, paddingHorizontal: 20, paddingVertical: 12 },
  buttonText: { color: "#fff", fontWeight: "600" },
  header: { flexDirection: "row", justifyContent: "space-between", padding: 12, backgroundColor: "#fff" },
  headerText: { color: "#666" },
  list: { padding: 12, gap: 10, flexGrow: 1 },
  card: { backgroundColor: "#fff", borderRadius: 12, padding: 14 },
  name: { fontSize: 16, fontWeight: "600", color: "#222" },
  distance: { fontSize: 13, color: "#e87c2a", marginTop: 4 },
});
