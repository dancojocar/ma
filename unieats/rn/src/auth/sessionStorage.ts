import * as SecureStore from "expo-secure-store";
import type { User } from "../domain/models";

const KEY = "unieats.session";
const OPTIONS: SecureStore.SecureStoreOptions = {
  keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
};

export interface StoredSession {
  token: string;
  user: User;
}

export async function saveSession(session: StoredSession) {
  await SecureStore.setItemAsync(KEY, JSON.stringify(session), OPTIONS);
}

export async function loadSession(): Promise<StoredSession | null> {
  const raw = await SecureStore.getItemAsync(KEY, OPTIONS);
  return raw ? (JSON.parse(raw) as StoredSession) : null;
}

export async function clearSession() {
  await SecureStore.deleteItemAsync(KEY, OPTIONS);
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
