import type { Spot } from "@unieats/shared";
import { useCallback, useEffect, useRef, useState } from "react";
import { AccessibilityInfo } from "react-native";
import { streamDishDescription } from "../api/aiClient";
import { ApiError, describeError, isUnauthorized } from "../api/errors";
import { useSessionStore } from "../store/sessionStore";

type Status = "idle" | "streaming" | "done" | "error";

function aiErrorMessage(e: unknown): string {
  if (!(e instanceof ApiError)) return "Could not describe this dish.";
  if (e.kind === "network" || e.kind === "timeout") return "Server unreachable. Is the UniEats server running?";
  if (e.status === 429) return "Too many requests. Try again in a minute.";
  return e.kind === "http" ? e.message : describeError(e);
}

export function useDishDescription(spot: Spot | null) {
  const [text, setText] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [error, setError] = useState<string | null>(null);
  const [source, setSource] = useState<string | null>(null);
  const controller = useRef<AbortController | null>(null);

  useEffect(() => () => controller.current?.abort(), []);

  const describe = useCallback(async () => {
    const { status: session, token } = useSessionStore.getState();
    if (!spot || session !== "signedIn" || !token) return;
    controller.current?.abort();
    const current = new AbortController();
    controller.current = current;
    setText("");
    setError(null);
    setSource(null);
    setStatus("streaming");
    let full = "";
    try {
      const from = await streamDishDescription(
        spot,
        token,
        (delta) => {
          full += delta;
          setText(full);
        },
        current.signal,
      );
      setSource(from);
      setStatus("done");
      // Announced once at the end: a live region on the growing text would re-read it for every chunk.
      AccessibilityInfo.announceForAccessibility(full);
    } catch (e) {
      if (current.signal.aborted) return;
      if (isUnauthorized(e)) {
        // No message here: this screen goes away and the login screen shows the expiry notice.
        await useSessionStore.getState().expire();
        return;
      }
      const message = aiErrorMessage(e);
      setError(message);
      setStatus("error");
      AccessibilityInfo.announceForAccessibility(message);
    }
  }, [spot]);

  return { text, status, error, source, describe };
}
