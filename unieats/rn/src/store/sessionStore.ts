import { create } from "zustand";
import { login as apiLogin } from "../api/client";
import { clearSession, isExpired, loadSession, saveSession } from "../auth/sessionStorage";
import type { User } from "../domain/models";

type SessionStatus = "restoring" | "signedOut" | "signedIn";

interface SessionState {
  status: SessionStatus;
  token: string | null;
  user: User | null;
  notice: string | null;
  restore: () => Promise<void>;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  expire: () => Promise<void>;
}

const signedOut = { status: "signedOut" as const, token: null, user: null };

/** The JWT lives in memory here; expo-secure-store only keeps it across app restarts. */
export const useSessionStore = create<SessionState>()((set, get) => ({
  status: "restoring",
  token: null,
  user: null,
  notice: null,

  restore: async () => {
    const stored = await loadSession().catch(() => null);
    if (stored && !isExpired(stored.token)) {
      set({ status: "signedIn", token: stored.token, user: stored.user });
    } else {
      if (stored) await clearSession();
      set(signedOut);
    }
  },

  login: async (email, password) => {
    const { token, user } = await apiLogin(email, password);
    await saveSession({ token, user });
    set({ status: "signedIn", token, user, notice: null });
  },

  logout: async () => {
    await clearSession();
    set({ ...signedOut, notice: null });
  },

  expire: async () => {
    if (get().status !== "signedIn") return;
    await clearSession();
    set({ ...signedOut, notice: "Your session expired. Please sign in again." });
  },
}));
