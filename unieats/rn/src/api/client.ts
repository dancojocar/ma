import { z } from "zod";
import type { Review, Spot, SpotCategory, SpotEdit } from "../domain/models";
import { ReviewSchema, SpotPageSchema, SpotSchema, type SpotPage } from "../domain/schemas";
import { API_URL } from "./config";
import { ApiError } from "./errors";

const TIMEOUT_MS = 10_000;
export const PAGE_SIZE = 20;

interface RequestOptions {
  method?: "GET" | "PATCH";
  body?: unknown;
  idempotencyKey?: string;
  signal?: AbortSignal;
}

async function request<T>(path: string, schema: z.ZodType<T>, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, idempotencyKey, signal } = options;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  const onCancel = () => controller.abort();
  signal?.addEventListener("abort", onCancel);

  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (idempotencyKey) headers["Idempotency-Key"] = idempotencyKey;

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: controller.signal,
    });
  } catch (e) {
    if (controller.signal.aborted && !signal?.aborted) {
      throw new ApiError("timeout", `${method} ${path} timed out`);
    }
    throw new ApiError("network", e instanceof Error ? e.message : String(e));
  } finally {
    clearTimeout(timer);
    signal?.removeEventListener("abort", onCancel);
  }

  let json: unknown;
  try {
    json = await response.json();
  } catch {
    throw new ApiError("parse", `${method} ${path}: body is not JSON`, response.status);
  }

  if (!response.ok) {
    const code = (json as { error?: { code?: string } } | null)?.error?.code;
    throw new ApiError("http", `${method} ${path} → ${response.status}`, response.status, code, json);
  }

  const parsed = schema.safeParse(json);
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
  return request(`/spots?${search.toString()}`, SpotPageSchema, { signal: params.signal });
}

export function getSpot(id: string, signal?: AbortSignal): Promise<Spot> {
  return request(`/spots/${encodeURIComponent(id)}`, SpotSchema, { signal });
}

export function getReviews(spotId: string, signal?: AbortSignal): Promise<Review[]> {
  return request(`/spots/${encodeURIComponent(spotId)}/reviews`, z.array(ReviewSchema), { signal });
}

/** `updatedAt` is the server version the edit was based on; the server answers 409 if its copy is newer. */
export function patchSpot(
  id: string,
  edit: SpotEdit & { updatedAt: number },
  idempotencyKey: string,
): Promise<Spot> {
  return request(`/spots/${encodeURIComponent(id)}`, SpotSchema, {
    method: "PATCH",
    body: edit,
    idempotencyKey,
  });
}

export function conflictServerCopy(error: unknown): Spot | null {
  if (!(error instanceof ApiError) || error.status !== 409) return null;
  const parsed = SpotSchema.safeParse((error.body as { spot?: unknown } | undefined)?.spot);
  return parsed.success ? parsed.data : null;
}
