import { StyleSheet, Text } from "react-native";
import type { LiveStatus } from "../hooks/useLiveSpots";

export function LiveBadge({ status }: { status: LiveStatus }) {
  const live = status === "live";
  return (
    <Text style={[styles.badge, live ? styles.live : styles.connecting]} testID="live-status">
      {live ? "● Live" : "○ Reconnecting…"}
    </Text>
  );
}

const styles = StyleSheet.create({
  badge: { fontSize: 12, fontWeight: "600" },
  live: { color: "#2e7d32" },
  connecting: { color: "#999" },
});
