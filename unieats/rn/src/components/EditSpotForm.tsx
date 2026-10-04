import { useState } from "react";
import { Pressable, StyleSheet, Switch, Text, TextInput, View } from "react-native";
import type { SpotEdit } from "../domain/models";
import { detailStyles } from "./SpotDetail";

export function EditSpotForm({
  initial,
  onSave,
  onCancel,
}: {
  initial: SpotEdit;
  onSave: (edit: SpotEdit) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState(initial.name);
  const [description, setDescription] = useState(initial.description);
  const [openNow, setOpenNow] = useState(initial.openNow);
  const valid = name.trim().length > 0;

  return (
    <View style={detailStyles.section}>
      <Text style={detailStyles.sectionTitle}>Edit spot</Text>
      <Text style={styles.label}>Name</Text>
      <TextInput style={styles.input} value={name} onChangeText={setName} testID="edit-name" />
      <Text style={styles.label}>Description</Text>
      <TextInput
        style={[styles.input, styles.multiline]}
        value={description}
        onChangeText={setDescription}
        multiline
      />
      <View style={styles.switchRow}>
        <Text style={styles.label}>Open now</Text>
        <Switch value={openNow} onValueChange={setOpenNow} />
      </View>
      <View style={styles.buttons}>
        <Pressable style={styles.secondary} onPress={onCancel}>
          <Text style={styles.secondaryText}>Cancel</Text>
        </Pressable>
        <Pressable
          style={[styles.primary, !valid && styles.disabled]}
          disabled={!valid}
          onPress={() => onSave({ name: name.trim(), description: description.trim(), openNow })}
          testID="edit-save"
        >
          <Text style={styles.primaryText}>Save</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  label: { fontSize: 13, color: "#666" },
  input: { borderWidth: 1, borderColor: "#ddd", borderRadius: 8, padding: 10, fontSize: 15 },
  multiline: { minHeight: 70, textAlignVertical: "top" },
  switchRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  buttons: { flexDirection: "row", justifyContent: "flex-end", gap: 12, marginTop: 4 },
  primary: { backgroundColor: "#e87c2a", borderRadius: 8, paddingHorizontal: 18, paddingVertical: 10 },
  primaryText: { color: "#fff", fontWeight: "600" },
  secondary: { paddingHorizontal: 12, paddingVertical: 10 },
  secondaryText: { color: "#e87c2a", fontWeight: "600" },
  disabled: { opacity: 0.5 },
});
