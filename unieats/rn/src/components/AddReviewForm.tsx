import { useState } from "react";
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { detailStyles } from "./SpotDetail";

export function AddReviewForm({
  onSubmit,
  submitting,
  error,
}: {
  onSubmit: (review: { stars: number; text: string }, reset: () => void) => void;
  submitting: boolean;
  error: string | null;
}) {
  const [stars, setStars] = useState(5);
  const [text, setText] = useState("");
  const reset = () => {
    setStars(5);
    setText("");
  };

  return (
    <View style={detailStyles.section}>
      <Text style={detailStyles.sectionTitle}>Add a review</Text>
      <View style={styles.stars}>
        {[1, 2, 3, 4, 5].map((n) => (
          <Pressable key={n} onPress={() => setStars(n)} hitSlop={4} accessibilityLabel={`${n} stars`}>
            <Text style={[styles.star, n <= stars && styles.starOn]}>★</Text>
          </Pressable>
        ))}
      </View>
      <TextInput
        style={styles.input}
        placeholder="What did you think?"
        value={text}
        onChangeText={setText}
        multiline
        editable={!submitting}
        testID="review-text"
      />
      {error && <Text style={styles.error}>{error}</Text>}
      <Pressable
        style={[styles.button, (submitting || !text.trim()) && styles.disabled]}
        disabled={submitting || !text.trim()}
        onPress={() => onSubmit({ stars, text: text.trim() }, reset)}
        testID="review-submit"
      >
        {submitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Post review</Text>}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  stars: { flexDirection: "row", gap: 6 },
  star: { fontSize: 28, color: "#ddd" },
  starOn: { color: "#f0a500" },
  input: { borderWidth: 1, borderColor: "#ddd", borderRadius: 8, padding: 10, minHeight: 70, textAlignVertical: "top" },
  error: { color: "#c0392b" },
  button: { backgroundColor: "#e87c2a", borderRadius: 8, padding: 12, alignItems: "center" },
  buttonText: { color: "#fff", fontWeight: "600" },
  disabled: { opacity: 0.5 },
});
