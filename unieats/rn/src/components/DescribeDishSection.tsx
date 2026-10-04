import type { Spot } from "@unieats/shared";
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import { useDishDescription } from "../hooks/useDishDescription";
import { detailStyles } from "./SpotDetail";

export function DescribeDishSection({ spot }: { spot: Spot }) {
  const { text, status, error, source, describe } = useDishDescription(spot);
  const streaming = status === "streaming";

  return (
    <View style={detailStyles.section}>
      <Pressable
        style={[styles.button, streaming && styles.disabled]}
        onPress={describe}
        disabled={streaming}
        accessibilityRole="button"
        testID="describe-dish-button"
      >
        <Text style={styles.buttonText}>Describe this dish</Text>
      </Pressable>
      {streaming && text === "" && <ActivityIndicator color="#e87c2a" />}
      {text !== "" && (
        <Text style={styles.text} testID="dish-description">
          {text}
        </Text>
      )}
      {source && <Text style={styles.source}>Source: {source}</Text>}
      {error && <Text style={styles.error}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  button: { backgroundColor: "#6a4bc4", borderRadius: 10, paddingVertical: 12, alignItems: "center" },
  disabled: { opacity: 0.6 },
  buttonText: { color: "#fff", fontWeight: "600", fontSize: 15 },
  text: { fontSize: 15, color: "#333", lineHeight: 22 },
  source: { fontSize: 12, color: "#999" },
  error: { color: "#c0392b" },
});
