import { StyleSheet, TextInput } from "react-native";

export function SearchBar({
  query,
  onQueryChange,
}: {
  query: string;
  onQueryChange: (query: string) => void;
}) {
  return (
    <TextInput
      style={styles.input}
      value={query}
      onChangeText={onQueryChange}
      placeholder="Search spots..."
      autoCorrect={false}
      clearButtonMode="while-editing"
      testID="search-input"
    />
  );
}

const styles = StyleSheet.create({
  input: {
    backgroundColor: "#fff",
    borderRadius: 10,
    borderWidth: 1,
    borderColor: "#ddd",
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 15,
  },
});
