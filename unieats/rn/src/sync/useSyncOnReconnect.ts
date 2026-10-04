import NetInfo from "@react-native-community/netinfo";
import { useEffect } from "react";
import { API_URL } from "../api/config";
import { syncPending } from "../repository/spotRepository";
import { useNetworkStore } from "./networkStore";

// "Reachable" should mean our server answers, not that Google does (the emulator may have no internet).
NetInfo.configure({
  reachabilityUrl: `${API_URL}/health`,
  reachabilityTest: async (response) => response.status === 200,
});

/** Mounted once at the root: replays the outbox at start-up and on every offline → online transition. */
export function useSyncOnReconnect() {
  const setOnline = useNetworkStore((s) => s.setOnline);

  useEffect(() => {
    let wasOnline = false;
    const unsubscribe = NetInfo.addEventListener((state) => {
      // isInternetReachable is null until the first probe completes; treat unknown as offline.
      const online = state.isConnected === true && state.isInternetReachable === true;
      setOnline(online);
      if (online && !wasOnline) void syncPending();
      wasOnline = online;
    });
    return unsubscribe;
  }, [setOnline]);
}
