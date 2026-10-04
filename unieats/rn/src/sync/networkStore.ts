import { create } from "zustand";

interface NetworkState {
  online: boolean;
  setOnline: (online: boolean) => void;
}

export const useNetworkStore = create<NetworkState>()((set) => ({
  online: true,
  setOnline: (online) => set({ online }),
}));
