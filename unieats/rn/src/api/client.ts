import { z } from "zod";
import type { Review, Spot, SpotCategory } from "../domain/models";
import { ReviewSchema, SpotPageSchema, SpotSchema, type SpotPage } from "../domain/schemas";
import { API_URL } from "./config";
import { ApiError } from "./errors";

const TIMEOUT_MS = 10_000;
export const PAGE_SIZE = 20;

async function getJson<T>(path: string, schema: z.ZodType<T>, signal?: AbortSignal): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  const onCancel = () => controller.abort();
  signal?.addEventListener("abort", onCancel);

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      headers: { Accept: "application/json" },
      signal: controller.signal,
    });
  } catch (e) {
    if (controller.signal.aborted && !signal?.aborted) {
      throw new ApiError("timeout", `GET ${path} timed out`);
    }
    throw new ApiError("network", e instanceof Error ? e.message : String(e));
  } finally {
    clearTimeout(timer);
    signal?.removeEventListener("abort", onCancel);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new ApiError("parse", `GET ${path}: body is not JSON`, response.status);
  }

  if (!response.ok) {
    const code = (body as { error?: { code?: string } } | null)?.error?.code;
    throw new ApiError("http", `GET ${path} → ${response.status}`, response.status, code);
  }

  const parsed = schema.safeParse(body);
  if (!parsed.success) {
    throw new ApiError("parse", parsed.error.message, response.status);
  }
  return parsed.data;
}

export function listSpots(params: {
  page: number;
  q: string;
  category: SpotCategory | "all";
  signal?: AbortSignal;
}): Promise<SpotPage> {
  const search = new URLSearchParams({ page: String(params.page), limit: String(PAGE_SIZE) });
  if (params.q) search.set("q", params.q);
  if (params.category !== "all") search.set("category", params.category);
  return getJson(`/spots?${search.toString()}`, SpotPageSchema, params.signal);
}

export function getSpot(id: string, signal?: AbortSignal): Promise<Spot> {
  return getJson(`/spots/${encodeURIComponent(id)}`, SpotSchema, signal);
}

export function getReviews(spotId: string, signal?: AbortSignal): Promise<Review[]> {
  return getJson(`/spots/${encodeURIComponent(spotId)}/reviews`, z.array(ReviewSchema), signal);
}
