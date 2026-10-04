import { useState } from "react";
import { Pressable, StyleSheet, Switch, Text, View } from "react-native";
import { describeError } from "../api/errors";
import { crashReporter, useCrashConsent } from "../cloud/crashReporting";
import { useFeatureFlags } from "../cloud/featureFlags";

export function SettingsSection() {
  const flags = useFeatureFlags((s) => s.flags);
  const lastFetchedAt = useFeatureFlags((s) => s.lastFetchedAt);
  const fetchAndActivate = useFeatureFlags((s) => s.fetchAndActivate);
  const consent = useCrashConsent((s) => s.consent);
  const setConsent = useCrashConsent((s) => s.setConsent);
  const [status, setStatus] = useState<string | null>(null);

  async function onFetch() {
    setStatus("Fetching…");
    try {
      await fetchAndActivate();
      setStatus(null);
    } catch (e) {
      setStatus(describeError(e));
    }
  }

  return (
    <View style={styles.card}>
      <Text style={styles.heading}>Remote config</Text>
      <Text style={styles.row} testID="flag-show-new-rating-ui">
        show_new_rating_ui: {String(flags.show_new_rating_ui)}
      </Text>
      <Text style={styles.muted}>
        {lastFetchedAt ? `Activated ${new Date(lastFetchedAt).toLocaleTimeString()}` : "Using shipped defaults"}
      </Text>
      {status && <Text style={styles.muted}>{status}</Text>}
      <Pressable style={styles.button} onPress={onFetch} testID="fetch-activate">
        <Text style={styles.buttonText}>Fetch & activate</Text>
      </Pressable>

      <Text style={[styles.heading, styles.spaced]}>Crash reporting</Text>
      <View style={styles.switchRow}>
        <Text style={styles.row}>Share crash reports</Text>
        <Switch value={consent} onValueChange={setConsent} testID="crash-consent" />
      </View>
      {__DEV__ && (
        <Pressable
          style={[styles.button, styles.danger]}
          onPress={() => crashReporter.recordError(new Error("Test crash from the Profile screen"))}
          testID="test-crash"
        >
          <Text style={styles.buttonText}>Test crash</Text>
        </Pressable>
      )}
      {__DEV__ && !consent && <Text style={styles.muted}>Consent is off, so the test crash is not reported.</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: "#fff", borderRadius: 12, padding: 16, gap: 8 },
  heading: { fontSize: 16, fontWeight: "700", color: "#222" },
  spaced: { marginTop: 12 },
  row: { fontSize: 14, color: "#333" },
  muted: { fontSize: 12, color: "#888" },
  switchRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  button: { backgroundColor: "#e87c2a", borderRadius: 8, padding: 10, alignItems: "center" },
  danger: { backgroundColor: "#c0392b" },
  buttonText: { color: "#fff", fontWeight: "600" },
});
