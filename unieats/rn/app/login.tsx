import { useRouter } from "expo-router";
import { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { ApiError, describeError } from "../src/api/errors";
import { useSessionStore } from "../src/store/sessionStore";

export default function LoginScreen() {
  const router = useRouter();
  const login = useSessionStore((s) => s.login);
  const notice = useSessionStore((s) => s.notice);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit() {
    if (!email.trim() || !password) {
      setError("Email and password are required.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await login(email.trim(), password);
      router.replace("/spots");
    } catch (e) {
      setError(e instanceof ApiError && e.status === 401 ? "Wrong email or password." : describeError(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <View style={styles.form}>
        <Text style={styles.title}>UniEats</Text>
        <Text style={styles.subtitle}>Sign in to edit spots and write reviews</Text>
        {notice && <Text style={styles.notice}>{notice}</Text>}
        <TextInput
          style={styles.input}
          placeholder="Email"
          autoCapitalize="none"
          autoComplete="email"
          keyboardType="email-address"
          value={email}
          onChangeText={setEmail}
          editable={!busy}
          testID="login-email"
        />
        <TextInput
          style={styles.input}
          placeholder="Password"
          secureTextEntry
          autoComplete="password"
          value={password}
          onChangeText={setPassword}
          onSubmitEditing={submit}
          editable={!busy}
          testID="login-password"
        />
        {error && <Text style={styles.error}>{error}</Text>}
        <Pressable
          style={[styles.button, busy && styles.disabled]}
          onPress={submit}
          disabled={busy}
          testID="login-submit"
        >
          {busy ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Sign in</Text>}
        </Pressable>
        <Text style={styles.hint}>Demo account: student@unieats.app / password</Text>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#fff", justifyContent: "center" },
  form: { padding: 24, gap: 12 },
  title: { fontSize: 36, fontWeight: "700", color: "#e87c2a", textAlign: "center" },
  subtitle: { fontSize: 15, color: "#666", textAlign: "center", marginBottom: 12 },
  notice: { color: "#8a4b00", backgroundColor: "#fff4e5", padding: 10, borderRadius: 8 },
  input: { borderWidth: 1, borderColor: "#ddd", borderRadius: 10, padding: 12, fontSize: 16 },
  error: { color: "#c0392b" },
  button: { backgroundColor: "#e87c2a", borderRadius: 10, padding: 14, alignItems: "center" },
  buttonText: { color: "#fff", fontSize: 16, fontWeight: "600" },
  disabled: { opacity: 0.6 },
  hint: { color: "#999", fontSize: 13, textAlign: "center", marginTop: 8 },
});
