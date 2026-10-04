import { View, Text, StyleSheet } from "react-native";

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>UniEats</Text>
      <Text style={styles.tagline}>Find your next campus meal.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#fff",
  },
  title: {
    fontSize: 36,
    fontWeight: "700",
    color: "#e87c2a",
    marginBottom: 12,
  },
  tagline: {
    fontSize: 16,
    color: "#666",
  },
});
