import { create } from "zustand";

export type Table = "spots" | "reviews" | "outbox";

const useTableVersions = create<Record<Table, number>>()(() => ({ spots: 0, reviews: 0, outbox: 0 }));

/** DAOs call this after every write so hooks re-read the tables they depend on. */
export function notifyChanged(...tables: Table[]) {
  useTableVersions.setState((versions) => {
    const next = { ...versions };
    for (const t of tables) next[t] += 1;
    return next;
  });
}

export function useTableVersion(table: Table): number {
  return useTableVersions((versions) => versions[table]);
}
