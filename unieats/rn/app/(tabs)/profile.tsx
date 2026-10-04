import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSessionStore } from "../../src/store/sessionStore";

export default function ProfileScreen() {
  const user = useSessionStore((s) => s.user);
  const logout = useSessionStore((s) => s.logout);

  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{user?.displayName[0]?.toUpperCase() ?? "?"}</Text>
        </View>
        <Text style={styles.name}>{user?.displayName}</Text>
        <Text style={styles.email}>{user?.email}</Text>
        <Pressable style={styles.logout} onPress={logout} testID="logout-button">
          <Text style={styles.logoutText}>Sign out</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5", padding: 16, gap: 12 },
  card: { backgroundColor: "#fff", borderRadius: 12, padding: 20, alignItems: "center", gap: 6 },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: "#e87c2a",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 6,
  },
  avatarText: { color: "#fff", fontSize: 30, fontWeight: "700" },
  name: { fontSize: 20, fontWeight: "700", color: "#222" },
  email: { fontSize: 14, color: "#666" },
  logout: { marginTop: 12, borderWidth: 1, borderColor: "#c0392b", borderRadius: 10, paddingHorizontal: 24, paddingVertical: 10 },
  logoutText: { color: "#c0392b", fontWeight: "600" },
});
