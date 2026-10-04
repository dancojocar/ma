import { create } from "zustand";
import { login as apiLogin } from "../api/client";
import { queryClient } from "../api/queryClient";
import { confirmBiometricOwner } from "../auth/biometrics";
import { forgetPendingLink } from "../auth/pendingLink";
import {
  clearSession,
  isBiometricSessionStored,
  isExpired,
  loadBiometricSession,
  loadSession,
  saveBiometricSession,
  saveSession,
  type StoredSession,
} from "../auth/sessionStorage";
import { discardAllLocalChanges, discardOtherUsersChanges } from "../db/localChanges";
import type { User } from "@unieats/shared";

export type SessionStatus = "restoring" | "locked" | "signedOut" | "signedIn";

interface SessionState {
  status: SessionStatus;
  token: string | null;
  user: User | null;
  notice: string | null;
  biometricEnabled: boolean;
  restore: () => Promise<void>;
  login: (email: string, password: string) => Promise<void>;
  unlock: () => Promise<void>;
  setBiometric: (enabled: boolean) => Promise<void>;
  logout: () => Promise<void>;
  expire: () => Promise<void>;
}

const EXPIRED_NOTICE = "Your session expired. Please sign in again.";
const BIOMETRICS_CHANGED_NOTICE = "Biometrics on this device changed. Sign in with your password.";

const signedOut = { status: "signedOut" as const, token: null, user: null, biometricEnabled: false };

function startSession(session: StoredSession, biometricEnabled: boolean) {
  if (discardOtherUsersChanges(session.user.id)) void queryClient.invalidateQueries();
  return { status: "signedIn" as const, ...session, biometricEnabled, notice: null };
}

/** The JWT lives in memory here; expo-secure-store only keeps it across app restarts. */
export const useSessionStore = create<SessionState>()((set, get) => ({
  status: "restoring",
  token: null,
  user: null,
  notice: null,
  biometricEnabled: false,

  restore: async () => {
    if (await isBiometricSessionStored().catch(() => false)) {
      set({ status: "locked", biometricEnabled: true });
      return;
    }
    const stored = await loadSession().catch(() => null);
    if (stored && !isExpired(stored.token)) {
      // A cold-start deep link was already routed into the tabs, so there is nothing left to open.
      forgetPendingLink();
      set(startSession(stored, false));
    } else {
      if (stored) await clearSession();
      set({ ...signedOut, notice: stored ? EXPIRED_NOTICE : null });
    }
  },

  login: async (email, password) => {
    const session = await apiLogin(email, password);
    await saveSession(session);
    set(startSession(session, false));
  },

  unlock: async () => {
    const stored = await loadBiometricSession();
    if (stored && !isExpired(stored.token)) {
      set(startSession(stored, true));
      return;
    }
    await clearSession();
    set({ ...signedOut, notice: stored ? EXPIRED_NOTICE : BIOMETRICS_CHANGED_NOTICE });
  },

  setBiometric: async (enabled) => {
    const { token, user } = get();
    if (!token || !user) return;
    if (enabled) {
      if (!(await confirmBiometricOwner())) return;
      await saveBiometricSession({ token, user });
    } else {
      await saveSession({ token, user });
    }
    set({ biometricEnabled: enabled });
  },

  logout: async () => {
    if (discardAllLocalChanges()) void queryClient.invalidateQueries();
    await clearSession();
    set({ ...signedOut, notice: null });
  },

  expire: async () => {
    if (get().status !== "signedIn") return;
    await clearSession();
    set({ ...signedOut, notice: EXPIRED_NOTICE });
  },
}));
