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
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onFetch() {
    setFetching(true);
    setError(null);
    try {
      await fetchAndActivate();
    } catch (e) {
      setError(describeError(e));
    } finally {
      setFetching(false);
    }
  }

  return (
    <View style={styles.card}>
      <Text style={styles.heading} accessibilityRole="header">
        Remote config
      </Text>
      <Text style={styles.row} testID="flag-show-new-rating-ui">
        show_new_rating_ui: {String(flags.show_new_rating_ui)}
      </Text>
      <Text style={styles.muted} accessibilityLiveRegion="polite">
        {fetching
          ? "Fetching…"
          : (error ??
            (lastFetchedAt ? `Activated ${new Date(lastFetchedAt).toLocaleTimeString()}` : "Using shipped defaults"))}
      </Text>
      <Pressable
        style={[styles.button, fetching && styles.disabled]}
        onPress={onFetch}
        disabled={fetching}
        accessibilityRole="button"
        accessibilityState={{ disabled: fetching }}
        testID="fetch-activate"
      >
        <Text style={styles.buttonText}>Fetch & activate</Text>
      </Pressable>

      <Text style={[styles.heading, styles.spaced]} accessibilityRole="header">
        Crash reporting
      </Text>
      <View style={styles.switchRow}>
        <Text style={styles.row}>Share crash reports</Text>
        <Switch
          value={consent}
          onValueChange={setConsent}
          accessibilityLabel="Share crash reports"
          testID="crash-consent"
        />
      </View>
      {__DEV__ && (
        <Pressable
          style={[styles.button, styles.danger]}
          onPress={() => crashReporter.recordError(new Error("Test crash from the Profile screen"))}
          accessibilityRole="button"
          accessibilityHint="Sends a test error to the crash reporter"
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
  muted: { fontSize: 12, color: "#666" },
  switchRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  button: { backgroundColor: "#e87c2a", borderRadius: 8, padding: 10, alignItems: "center" },
  disabled: { opacity: 0.6 },
  danger: { backgroundColor: "#c0392b" },
  buttonText: { color: "#fff", fontWeight: "600" },
});
