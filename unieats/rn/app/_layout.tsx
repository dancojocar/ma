import { QueryClientProvider } from "@tanstack/react-query";
import { useEffect } from "react";
import { Stack, type ErrorBoundaryProps } from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import { StatusBar } from "expo-status-bar";
import { StyleSheet } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { queryClient } from "../src/api/queryClient";
import { installGlobalErrorHandler, loadCrashConsent } from "../src/cloud/crashReporting";
import { ErrorFallback } from "../src/components/ErrorFallback";
import { useSessionStore } from "../src/store/sessionStore";
import { useSyncOnReconnect } from "../src/sync/useSyncOnReconnect";

void SplashScreen.preventAutoHideAsync();
// At module load rather than in an effect: if the first render throws, the layout's effects never run,
// and the ErrorBoundary below would report without knowing the user's consent.
loadCrashConsent();
installGlobalErrorHandler();

export default function RootLayout() {
  const status = useSessionStore((s) => s.status);
  const restore = useSessionStore((s) => s.restore);
  useEffect(() => {
    void restore();
  }, [restore]);
  useEffect(() => {
    if (status !== "restoring") void SplashScreen.hideAsync();
  }, [status]);
  useSyncOnReconnect();

  // When a guard turns false the stack falls back to its first screen, `index`, which redirects by status.
  // (tabs) stays allowed while restoring so a cold-start deep link with a stored session lands directly.
  return (
    <GestureHandlerRootView style={styles.flex}>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="auto" />
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="index" />
          <Stack.Protected guard={status === "signedIn" || status === "restoring"}>
            <Stack.Screen name="(tabs)" />
          </Stack.Protected>
          <Stack.Protected guard={status === "locked"}>
            <Stack.Screen name="unlock" />
          </Stack.Protected>
          <Stack.Protected guard={status === "signedOut"}>
            <Stack.Screen name="login" />
          </Stack.Protected>
        </Stack>
      </QueryClientProvider>
    </GestureHandlerRootView>
  );
}

export function ErrorBoundary(props: ErrorBoundaryProps) {
  // RootLayout, which normally hides the splash, is not mounted while this screen shows.
  useEffect(() => {
    void SplashScreen.hideAsync();
  }, []);
  return <ErrorFallback {...props} boundary="root" />;
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
});
