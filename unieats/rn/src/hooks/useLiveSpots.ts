import { useQueryClient } from "@tanstack/react-query";
import { useFocusEffect } from "expo-router";
import { useCallback, useRef, useState } from "react";
import { LIVE_URL } from "../api/config";
import { LiveEventSchema } from "../domain/schemas";
import { notifySpotUpdated } from "../notifications/spotNotifications";
import { applyLiveEvent } from "../repository/spotRepository";
import { useAppActive } from "./useAppActive";

export type LiveStatus = "connecting" | "live";

const MAX_BACKOFF_MS = 30_000;

/** Connected only while the calling screen is focused and the app is in the foreground. */
export function useLiveSpots(): LiveStatus {
  const queryClient = useQueryClient();
  const appActive = useAppActive();
  const [status, setStatus] = useState<LiveStatus>("connecting");
  const connectedBefore = useRef(false);

  useFocusEffect(
    useCallback(() => {
      if (!appActive) return;

      let socket: WebSocket | null = null;
      let retryTimer: ReturnType<typeof setTimeout> | undefined;
      let attempt = 0;
      let disposed = false;

      const connect = () => {
        setStatus("connecting");
        socket = new WebSocket(LIVE_URL);
        socket.onopen = () => {
          attempt = 0;
          setStatus("live");
          // Events sent while we were disconnected are lost; refetch the loaded pages into SQLite.
          if (connectedBefore.current) queryClient.invalidateQueries({ queryKey: ["spots"] });
          connectedBefore.current = true;
        };
        socket.onmessage = (message) => {
          try {
            const parsed = LiveEventSchema.safeParse(JSON.parse(String(message.data)));
            if (!parsed.success) return;
            const isNews = applyLiveEvent(parsed.data);
            if (isNews && parsed.data.type === "spot.updated") void notifySpotUpdated(parsed.data.spot);
          } catch {
            // Ignore frames that are not JSON; the next valid event still applies.
          }
        };
        socket.onclose = () => {
          if (disposed) return;
          setStatus("connecting");
          const delay = Math.min(MAX_BACKOFF_MS, 1000 * 2 ** attempt);
          attempt += 1;
          retryTimer = setTimeout(connect, delay);
        };
      };

      connect();
      return () => {
        disposed = true;
        clearTimeout(retryTimer);
        socket?.close();
      };
    }, [appActive, queryClient]),
  );

  return status;
}
