import type { Spot } from "@unieats/shared";
import { useCallback, useEffect, useRef, useState } from "react";
import { streamDishDescription } from "../api/aiClient";
import { ApiError, isUnauthorized } from "../api/errors";
import { useSessionStore } from "../store/sessionStore";

type Status = "idle" | "streaming" | "done" | "error";

function aiErrorMessage(e: unknown): string {
  if (e instanceof ApiError && (e.kind === "network" || e.kind === "timeout")) {
    return "Server unreachable. Is the UniEats server running?";
  }
  if (e instanceof ApiError && e.status === 429) return "Too many requests. Try again in a minute.";
  return e instanceof Error ? e.message : "Could not describe this dish.";
}

export function useDishDescription(spot: Spot | null) {
  const [text, setText] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [error, setError] = useState<string | null>(null);
  const [source, setSource] = useState<string | null>(null);
  const controller = useRef<AbortController | null>(null);

  useEffect(() => () => controller.current?.abort(), []);

  const describe = useCallback(async () => {
    const token = useSessionStore.getState().token;
    if (!spot || !token) return;
    controller.current?.abort();
    const current = new AbortController();
    controller.current = current;
    setText("");
    setError(null);
    setSource(null);
    setStatus("streaming");
    try {
      const from = await streamDishDescription(spot, token, (delta) => setText((t) => t + delta), current.signal);
      setSource(from);
      setStatus("done");
    } catch (e) {
      if (current.signal.aborted) return;
      if (isUnauthorized(e)) await useSessionStore.getState().expire();
      setError(aiErrorMessage(e));
      setStatus("error");
    }
  }, [spot]);

  return { text, status, error, source, describe };
}
