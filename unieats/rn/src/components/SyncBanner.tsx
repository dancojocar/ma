import { StyleSheet, Text, View } from "react-native";
import { usePendingChangesCount } from "../db/hooks";
import { useNetworkStore } from "../sync/networkStore";

export function SyncBanner() {
  const pending = usePendingChangesCount();
  const online = useNetworkStore((s) => s.online);
  if (online && pending === 0) return null;

  const parts = [];
  if (!online) parts.push("Offline");
  if (pending > 0) parts.push(`${pending} ${pending === 1 ? "change" : "changes"} pending`);

  return (
    <View style={[styles.banner, !online && styles.offline]} testID="sync-banner">
      <Text style={styles.text}>{parts.join(" · ")}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: { backgroundColor: "#fff4e5", paddingVertical: 6, paddingHorizontal: 12 },
  offline: { backgroundColor: "#fdecea" },
  text: { fontSize: 13, color: "#8a4b00", textAlign: "center" },
});
