import { SpotSchema, type Spot } from "@unieats/shared";
import { fetch } from "expo/fetch";
import { API_URL } from "./config";
import { ApiError } from "./errors";
import { createSseParser } from "./sse";

const CONNECT_TIMEOUT_MS = 10_000;

interface StreamPayload {
  delta?: string;
  source?: string;
  error?: { code?: string; message?: string };
}

/**
 * POST /api/ai/describe and stream the answer: `onDelta` gets each text chunk as it arrives.
 * Resolves with the `source` from the final `done` event ("template" offline, else the model id).
 * Uses expo/fetch because React Native's built-in fetch cannot read a response body incrementally.
 */
export async function streamDishDescription(
  spot: Spot,
  token: string,
  onDelta: (text: string) => void,
  signal?: AbortSignal,
): Promise<string> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), CONNECT_TIMEOUT_MS);
  const onCancel = () => controller.abort();
  signal?.addEventListener("abort", onCancel);

  try {
    let response;
    try {
      response = await fetch(`${API_URL}/ai/describe`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "text/event-stream",
          Authorization: `Bearer ${token}`,
        },
        // parse() strips local-only fields such as pendingSync; the server gets exactly a contract Spot.
        body: JSON.stringify({ spot: SpotSchema.parse(spot) }),
        signal: controller.signal,
      });
    } catch (e) {
      if (controller.signal.aborted && !signal?.aborted) {
        throw new ApiError("timeout", "POST /ai/describe timed out");
      }
      throw new ApiError("network", e instanceof Error ? e.message : String(e));
    } finally {
      // The server sends its headers at once; only the wait for them is timed, not the answer.
      clearTimeout(timer);
    }

    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as StreamPayload | null;
      throw new ApiError("http", body?.error?.message ?? `HTTP ${response.status}`, response.status, body?.error?.code);
    }
    if (!response.body) throw new ApiError("parse", "Streaming is not supported by this response");

    let source: string | null = null;
    let failure: ApiError | null = null;
    const feed = createSseParser(({ event, data }) => {
      let payload: StreamPayload;
      try {
        payload = JSON.parse(data) as StreamPayload;
      } catch {
        throw new ApiError("parse", `Event "${event}" is not JSON`);
      }
      if (event === "message" && payload.delta) onDelta(payload.delta);
      else if (event === "done") source = payload.source ?? "unknown";
      else if (event === "error") {
        failure = new ApiError("http", payload.error?.message ?? "AI unavailable", 503, payload.error?.code);
      }
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    for (;;) {
      let chunk;
      try {
        chunk = await reader.read();
      } catch (e) {
        throw new ApiError("network", e instanceof Error ? e.message : String(e));
      }
      if (chunk.done) break;
      feed(decoder.decode(chunk.value, { stream: true }));
    }
    feed(decoder.decode());

    if (failure) throw failure;
    if (source === null) throw new ApiError("parse", "The stream ended before the done event");
    return source;
  } finally {
    signal?.removeEventListener("abort", onCancel);
  }
}
