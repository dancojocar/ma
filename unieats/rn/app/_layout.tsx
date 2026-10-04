import { QueryClientProvider } from "@tanstack/react-query";
import { useEffect } from "react";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { StyleSheet } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { queryClient } from "../src/api/queryClient";
import { useSessionStore } from "../src/store/sessionStore";
import { useSyncOnReconnect } from "../src/sync/useSyncOnReconnect";

export default function RootLayout() {
  const restore = useSessionStore((s) => s.restore);
  useEffect(() => {
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

const styles = StyleSheet.create({
  flex: { flex: 1 },
});
