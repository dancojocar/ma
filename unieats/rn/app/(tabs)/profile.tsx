import { useEffect, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Switch, Text, View } from "react-native";
import { biometricSupport, type BiometricSupport } from "../../src/auth/biometrics";
import { routeErrorBoundary } from "../../src/components/ErrorFallback";
import { SettingsSection } from "../../src/components/SettingsSection";
import { usePendingChangesCount } from "../../src/db/hooks";
import { useSessionStore } from "../../src/store/sessionStore";

export const ErrorBoundary = routeErrorBoundary("profile");

export default function ProfileScreen() {
  const user = useSessionStore((s) => s.user);
  const logout = useSessionStore((s) => s.logout);
  const pendingChanges = usePendingChangesCount();

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.card}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{user?.displayName[0]?.toUpperCase() ?? "?"}</Text>
        </View>
        <Text style={styles.name}>{user?.displayName}</Text>
        <Text style={styles.email}>{user?.email}</Text>
        <Pressable style={styles.logout} onPress={logout} accessibilityRole="button" testID="logout-button">
          <Text style={styles.logoutText}>Sign out</Text>
        </Pressable>
        {pendingChanges > 0 && (
          <Text style={styles.warning}>
            {pendingChanges === 1
              ? "1 unsynced change will be discarded when you sign out."
              : `${pendingChanges} unsynced changes will be discarded when you sign out.`}
          </Text>
        )}
      </View>
      <BiometricRow />
      <SettingsSection />
    </ScrollView>
  );
}

function BiometricRow() {
  const enabled = useSessionStore((s) => s.biometricEnabled);
  const setBiometric = useSessionStore((s) => s.setBiometric);
  const [support, setSupport] = useState<BiometricSupport | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    biometricSupport()
      .then((s) => {
        if (active) setSupport(s);
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, []);

  if (!support?.available) return null;

  async function toggle(on: boolean) {
    setBusy(true);
    setError(null);
    try {
      await setBiometric(on);
    } catch {
      setError("Biometric unlock was not changed.");
    } finally {
      setBusy(false);
    }
  }

  const label = `Unlock with ${support.label}`;
  return (
    <View style={styles.card}>
      <View style={styles.row}>
        <Text style={styles.rowLabel}>{label}</Text>
        <Switch
          value={enabled}
          onValueChange={toggle}
          disabled={busy}
          accessibilityLabel={label}
          testID="biometric-switch"
        />
      </View>
      <Text style={styles.hint}>UniEats will ask for {support.label} every time it starts.</Text>
      {error && <Text style={styles.error}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  content: { padding: 16, gap: 12 },
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
  warning: { fontSize: 13, color: "#8a4b00", textAlign: "center" },
  row: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", alignSelf: "stretch" },
  rowLabel: { fontSize: 16, color: "#222" },
  hint: { fontSize: 13, color: "#666", alignSelf: "stretch" },
  error: { color: "#c0392b", alignSelf: "stretch" },
});
