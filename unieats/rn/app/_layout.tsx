import { QueryClientProvider } from "@tanstack/react-query";
import { useEffect } from "react";
import { Stack, type ErrorBoundaryProps } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { queryClient } from "../src/api/queryClient";
import { crashReporter, installGlobalErrorHandler, loadCrashConsent } from "../src/cloud/crashReporting";
import { useSessionStore } from "../src/store/sessionStore";
import { useSyncOnReconnect } from "../src/sync/useSyncOnReconnect";

export default function RootLayout() {
  const restore = useSessionStore((s) => s.restore);
  useEffect(() => {
    loadCrashConsent();
    installGlobalErrorHandler();
    void restore();
  }, [restore]);
  useSyncOnReconnect();

  return (
    <GestureHandlerRootView style={styles.flex}>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="auto" />
        <Stack>
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        </Stack>
      </QueryClientProvider>
    </GestureHandlerRootView>
  );
}

export function ErrorBoundary({ error, retry }: ErrorBoundaryProps) {
  useEffect(() => {
    crashReporter.recordError(error, { boundary: "root" });
  }, [error]);

  return (
    <View style={styles.errorScreen}>
      <Text style={styles.errorTitle}>Something went wrong</Text>
      <Text style={styles.errorText}>{error.message}</Text>
      <Pressable style={styles.retry} onPress={retry}>
        <Text style={styles.retryText}>Try again</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  errorScreen: { flex: 1, justifyContent: "center", alignItems: "center", padding: 24, gap: 12 },
  errorTitle: { fontSize: 20, fontWeight: "700" },
  errorText: { color: "#666", textAlign: "center" },
  retry: { backgroundColor: "#e87c2a", borderRadius: 8, paddingHorizontal: 20, paddingVertical: 10 },
  retryText: { color: "#fff", fontWeight: "600" },
});
