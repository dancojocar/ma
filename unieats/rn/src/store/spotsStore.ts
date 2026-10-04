import { create } from "zustand";
import type { SpotCategory } from "@unieats/shared";

export type CategoryFilter = SpotCategory | "all";

interface SpotsState {
  searchQuery: string;
  categoryFilter: CategoryFilter;
  favouriteIds: Set<string>;
  hiddenIds: Set<string>;
  setSearchQuery: (query: string) => void;
  setCategoryFilter: (category: CategoryFilter) => void;
  toggleFavourite: (id: string) => void;
  hide: (id: string) => void;
  showHidden: () => void;
}

export const useSpotsStore = create<SpotsState>()((set) => ({
  searchQuery: "",
  categoryFilter: "all",
  favouriteIds: new Set<string>(),
  hiddenIds: new Set<string>(),

  setSearchQuery: (searchQuery) => set({ searchQuery }),
  setCategoryFilter: (categoryFilter) => set({ categoryFilter }),
  toggleFavourite: (id) =>
    set((state) => {
      const next = new Set(state.favouriteIds);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return { favouriteIds: next };
    }),
  hide: (id) => set((state) => ({ hiddenIds: new Set(state.hiddenIds).add(id) })),
  showHidden: () => set({ hiddenIds: new Set<string>() }),
}));
