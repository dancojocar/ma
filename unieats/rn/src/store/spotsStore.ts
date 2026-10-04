import { create } from "zustand";
import type { SpotCategory } from "../domain/models";

export type CategoryFilter = SpotCategory | "all";

interface SpotsState {
  searchQuery: string;
  categoryFilter: CategoryFilter;
  favouriteIds: Set<string>;
  setSearchQuery: (query: string) => void;
  setCategoryFilter: (category: CategoryFilter) => void;
  toggleFavourite: (id: string) => void;
}

export const useSpotsStore = create<SpotsState>()((set) => ({
  searchQuery: "",
  categoryFilter: "all",
  favouriteIds: new Set<string>(),

  setSearchQuery: (searchQuery) => set({ searchQuery }),
  setCategoryFilter: (categoryFilter) => set({ categoryFilter }),
  toggleFavourite: (id) =>
    set((state) => {
      const next = new Set(state.favouriteIds);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return { favouriteIds: next };
    }),
}));
