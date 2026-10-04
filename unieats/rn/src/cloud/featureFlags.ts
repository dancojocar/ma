import { create } from "zustand";
import { getRemoteConfig } from "../api/client";
import type { RemoteConfig } from "@unieats/shared";

export type Flags = RemoteConfig["flags"];

export const DEFAULT_FLAGS: Flags = { show_new_rating_ui: false };

interface FlagsState {
  flags: Flags;
  lastFetchedAt: number | null;
  fetchAndActivate: () => Promise<void>;
}

/** Remote config: shipped defaults until "Fetch & activate" applies the server's values. */
export const useFeatureFlags = create<FlagsState>()((set) => ({
  flags: DEFAULT_FLAGS,
  lastFetchedAt: null,
  fetchAndActivate: async () => {
    const { flags } = await getRemoteConfig();
    set({ flags: { ...DEFAULT_FLAGS, ...flags }, lastFetchedAt: Date.now() });
  },
}));

export function useFlag<K extends keyof Flags>(key: K): Flags[K] {
  return useFeatureFlags((s) => s.flags[key]);
}
