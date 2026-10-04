import * as SecureStore from "expo-secure-store";
import type { User } from "../domain/models";

const KEY = "unieats.session";
const OPTIONS: SecureStore.SecureStoreOptions = {
  keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
};

const BIOMETRIC_FLAG_KEY = "unieats.session.biometric";
const BIOMETRIC_KEY = "unieats.biometricSession";
// requireAuthentication needs its own keychainService: a fresh Keystore key on Android, and on iOS a lookup
// without one would find the plain item first.
const BIOMETRIC_OPTIONS: SecureStore.SecureStoreOptions = {
  keychainService: "unieats.biometric",
  requireAuthentication: true,
  authenticationPrompt: "Unlock UniEats",
  keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
};

export interface StoredSession {
  token: string;
  user: User;
}

/** Stores the session without biometrics and removes any biometric copy. */
export async function saveSession(session: StoredSession) {
  await SecureStore.setItemAsync(KEY, JSON.stringify(session), OPTIONS);
  await SecureStore.deleteItemAsync(BIOMETRIC_FLAG_KEY, OPTIONS);
  await SecureStore.deleteItemAsync(BIOMETRIC_KEY, BIOMETRIC_OPTIONS);
}

export async function loadSession(): Promise<StoredSession | null> {
  const raw = await SecureStore.getItemAsync(KEY, OPTIONS);
  return raw ? (JSON.parse(raw) as StoredSession) : null;
}

/**
 * Moves the session into an item only biometrics can read. Android shows the prompt here (the Keystore key
 * needs it to encrypt); iOS creates the item without prompting.
 */
export async function saveBiometricSession(session: StoredSession) {
  // Deleting first makes this a create on iOS; updating an existing protected item would prompt.
  await SecureStore.deleteItemAsync(BIOMETRIC_KEY, BIOMETRIC_OPTIONS);
  await SecureStore.setItemAsync(BIOMETRIC_KEY, JSON.stringify(session), BIOMETRIC_OPTIONS);
  await SecureStore.setItemAsync(BIOMETRIC_FLAG_KEY, "1", OPTIONS);
  await SecureStore.deleteItemAsync(KEY, OPTIONS);
}

/** Readable without a prompt, so start-up knows whether to show the unlock screen. */
export async function isBiometricSessionStored(): Promise<boolean> {
  return (await SecureStore.getItemAsync(BIOMETRIC_FLAG_KEY, OPTIONS)) === "1";
}

/**
 * Prompts for biometrics. Resolves null when the key was invalidated because biometric enrollment changed;
 * rejects when the user cancels or authentication fails.
 */
export async function loadBiometricSession(): Promise<StoredSession | null> {
  const raw = await SecureStore.getItemAsync(BIOMETRIC_KEY, BIOMETRIC_OPTIONS);
  return raw ? (JSON.parse(raw) as StoredSession) : null;
}

export async function clearSession() {
  await SecureStore.deleteItemAsync(KEY, OPTIONS);
  await SecureStore.deleteItemAsync(BIOMETRIC_FLAG_KEY, OPTIONS);
  await SecureStore.deleteItemAsync(BIOMETRIC_KEY, BIOMETRIC_OPTIONS);
}

/** Reads `exp` without verifying the signature: only used to skip restoring a token the server would reject. */
export function isExpired(token: string, nowMs = Date.now()): boolean {
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const { exp } = JSON.parse(atob(payload)) as { exp?: number };
    return typeof exp !== "number" || exp * 1000 <= nowMs;
  } catch {
    return true;
  }
}
