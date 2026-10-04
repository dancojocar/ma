import type { ErrorBoundaryProps } from "expo-router";
import { useEffect } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { crashReporter } from "../cloud/crashReporting";

export function ErrorFallback({ error, retry, boundary }: ErrorBoundaryProps & { boundary: string }) {
  useEffect(() => {
    crashReporter.recordError(error, { boundary });
  }, [error, boundary]);

  return (
    <View style={styles.screen}>
      <Text style={styles.title} accessibilityRole="header">
        Something went wrong
      </Text>
      <Text style={styles.text}>{error.message}</Text>
      <Pressable style={styles.retry} onPress={retry} accessibilityRole="button" testID="error-retry">
        <Text style={styles.retryText}>Try again</Text>
      </Pressable>
    </View>
  );
}

export function routeErrorBoundary(boundary: string) {
  return function RouteErrorBoundary(props: ErrorBoundaryProps) {
    return <ErrorFallback {...props} boundary={boundary} />;
  };
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 24,
    gap: 12,
    backgroundColor: "#f5f5f5",
  },
  title: { fontSize: 20, fontWeight: "700", color: "#222" },
  text: { color: "#666", textAlign: "center" },
  retry: { backgroundColor: "#e87c2a", borderRadius: 8, paddingHorizontal: 20, paddingVertical: 10 },
  retryText: { color: "#fff", fontWeight: "600" },
});
