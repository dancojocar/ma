import { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import { usePendingChangesCount } from "../src/db/hooks";
import { useSessionStore } from "../src/store/sessionStore";

export default function UnlockScreen() {
  const unlock = useSessionStore((s) => s.unlock);
  const logout = useSessionStore((s) => s.logout);
  const pendingChanges = usePendingChangesCount();
  const [busy, setBusy] = useState(true);
  const [failed, setFailed] = useState(false);

  const tryUnlock = useCallback(async () => {
    setBusy(true);
    setFailed(false);
    try {
      await unlock();
    } catch {
      setFailed(true);
    } finally {
      setBusy(false);
    }
  }, [unlock]);

  useEffect(() => {
    void tryUnlock();
  }, [tryUnlock]);

  return (
    <View style={styles.container}>
      <Text style={styles.title} accessibilityRole="header">
        Unlock UniEats
      </Text>
      {busy && <ActivityIndicator color="#e87c2a" accessibilityLabel="Waiting for biometrics" />}
      {failed && (
        <>
          <Text style={styles.message}>Biometric unlock was cancelled or did not match.</Text>
          <Pressable style={styles.button} onPress={tryUnlock} accessibilityRole="button" testID="unlock-retry">
            <Text style={styles.buttonText}>Try again</Text>
          </Pressable>
          <Pressable
            style={styles.secondary}
            onPress={logout}
            accessibilityRole="button"
            testID="unlock-use-password"
          >
            <Text style={styles.secondaryText}>Use password instead</Text>
          </Pressable>
          {pendingChanges > 0 && (
            <Text style={styles.warning}>
              {pendingChanges === 1
                ? "1 unsynced change will be discarded."
                : `${pendingChanges} unsynced changes will be discarded.`}
            </Text>
          )}
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#fff", justifyContent: "center", padding: 24, gap: 14 },
  title: { fontSize: 28, fontWeight: "700", color: "#e87c2a", textAlign: "center", marginBottom: 8 },
  message: { fontSize: 15, color: "#666", textAlign: "center" },
  button: { backgroundColor: "#e87c2a", borderRadius: 10, padding: 14, alignItems: "center" },
  buttonText: { color: "#fff", fontSize: 16, fontWeight: "600" },
  secondary: { borderWidth: 1, borderColor: "#e87c2a", borderRadius: 10, padding: 14, alignItems: "center" },
  secondaryText: { color: "#e87c2a", fontSize: 16, fontWeight: "600" },
  warning: { fontSize: 13, color: "#8a4b00", textAlign: "center" },
});
