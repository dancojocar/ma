import {
  resolveConflict,
  type LiveEvent,
  type OutboxOp,
  type Review,
  type Spot,
  type SpotEdit,
  type SpotPage,
} from "@unieats/shared";
import { randomUUID } from "expo-crypto";
import {
  conflictServerCopy,
  getReviews,
  getSpot,
  listSpots,
  patchSpot,
  postReview,
} from "../api/client";
import { ApiError, isUnauthorized } from "../api/errors";
import { notifyChanged } from "../db/changes";
import { getDb } from "../db/database";
import * as outboxDao from "../db/outboxDao";
import { upsertReviews } from "../db/reviewDao";
import {
  applyLocalEdit,
  deleteIfNotPending,
  deleteSpot,
  getPendingSync,
  getSpotById,
  markSynced,
  recordServerVersion,
  upsertFromServer,
} from "../db/spotDao";
import { useSessionStore } from "../store/sessionStore";
import type { CategoryFilter } from "../store/spotsStore";

export async function refreshSpotsPage(params: {
  page: number;
  q: string;
  category: CategoryFilter;
  signal?: AbortSignal;
}): Promise<SpotPage> {
  const page = await listSpots(params);
  upsertFromServer(page.spots);
  return page;
}

export async function refreshSpot(id: string, signal?: AbortSignal): Promise<Spot> {
  try {
    const spot = await getSpot(id, signal);
    upsertFromServer([spot]);
    return spot;
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) deleteIfNotPending(id);
    throw e;
  }
}

export async function refreshReviews(spotId: string, signal?: AbortSignal): Promise<Review[]> {
  const reviews = await getReviews(spotId, signal);
  upsertReviews(reviews);
  return reviews;
}

export async function addReview(spotId: string, review: { stars: number; text: string }) {
  const token = useSessionStore.getState().token;
  if (!token) throw new ApiError("http", "Not signed in", 401);
  try {
    const created = await postReview(spotId, review, { token, idempotencyKey: randomUUID() });
    upsertReviews([created]);
    return created;
  } catch (e) {
    if (isUnauthorized(e)) await useSessionStore.getState().expire();
    throw e;
  }
}

/** Writes a live event into SQLite; returns true when it is news to this device (not an echo of our own write). */
export function applyLiveEvent(event: LiveEvent): boolean {
  if (event.type === "spot.deleted") {
    deleteIfNotPending(event.id);
    return false;
  }
  const local = getSpotById(event.spot.id);
  const isEcho =
    local !== null &&
    (local.updatedAt === event.spot.updatedAt ||
      (local.pendingSync &&
        local.name === event.spot.name &&
        local.description === event.spot.description &&
        local.openNow === event.spot.openNow));
  upsertFromServer([event.spot]);
  return !isEcho;
}

/** Optimistic: the UI shows the edit immediately; the outbox replays it when the server is reachable. */
export function editSpot(id: string, edit: SpotEdit) {
  const base = getSpotById(id);
  if (!base) return;
  getDb().withTransactionSync(() => {
    applyLocalEdit(id, edit);
    outboxDao.enqueue({
      opId: randomUUID(),
      type: "update",
      entityId: id,
      payload: { ...edit, updatedAt: base.updatedAt },
      createdAt: Date.now(),
    });
  });
  notifyChanged("spots", "outbox");
  void syncPending();
}

let running: Promise<void> | null = null;
let rerun = false;

/** Replays the outbox in order. Concurrent callers share one run; a call during a run schedules one more pass. */
export function syncPending(): Promise<void> {
  if (running) {
    rerun = true;
    return running;
  }
  running = (async () => {
    do {
      rerun = false;
      await replayOutbox();
    } while (rerun);
  })().finally(() => {
    running = null;
  });
  return running;
}

type ReplayResult = { kind: "synced"; spot: Spot } | { kind: "dropped" } | { kind: "retryLater" };

async function replayOutbox() {
  const token = useSessionStore.getState().token;
  if (!token) return;
  const serverCopies = new Map<string, Spot>();

  // Re-read the head each time: a successful write rebases the queued ops behind it.
  for (let op = outboxDao.oldest(); op; op = outboxDao.oldest()) {
    const result = await replay(op, token);
    if (result.kind === "retryLater") break;
    if (result.kind === "synced") serverCopies.set(op.entityId, result.spot);
    outboxDao.remove(op.seq);
  }

  for (const spot of getPendingSync()) {
    if (outboxDao.hasOpsFor(spot.id)) continue;
    const known = serverCopies.get(spot.id);
    if (known) {
      markSynced(known);
      continue;
    }
    try {
      markSynced(await getSpot(spot.id));
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) deleteSpot(spot.id);
    }
  }
}

async function replay(op: OutboxOp, token: string): Promise<ReplayResult> {
  try {
    return synced(op.entityId, await patchSpot(op.entityId, op.payload, { token, idempotencyKey: op.opId }));
  } catch (e) {
    if (isUnauthorized(e)) {
      await useSessionStore.getState().expire();
      return { kind: "retryLater" };
    }
    const serverCopy = conflictServerCopy(e);
    if (serverCopy) {
      // The server answers 409 only when its copy is strictly newer, i.e. last-write-wins picks the server.
      if (resolveConflict(op.payload.updatedAt, serverCopy.updatedAt) === "server") {
        return { kind: "synced", spot: serverCopy };
      }
      return { kind: "dropped" };
    }
    if (e instanceof ApiError && e.kind === "http" && e.status !== undefined && e.status < 500) {
      console.warn(`Dropping outbox op ${op.opId}: ${e.message}`);
      return { kind: "dropped" };
    }
    return { kind: "retryLater" };
  }
}

/** Our own write was accepted, so queued edits of the same spot are now based on its new version. */
function synced(id: string, spot: Spot): ReplayResult {
  recordServerVersion(id, spot.updatedAt);
  outboxDao.rebase(id, spot.updatedAt);
  return { kind: "synced", spot };
}
