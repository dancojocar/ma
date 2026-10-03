import { randomUUID } from "expo-crypto";
import { conflictServerCopy, getReviews, getSpot, listSpots, patchSpot } from "../api/client";
import { ApiError } from "../api/errors";
import { notifyChanged } from "../db/changes";
import { getDb } from "../db/database";
import * as outboxDao from "../db/outboxDao";
import { upsertReviews } from "../db/reviewDao";
import {
  applyLocalEdit,
  deleteIfNotPending,
  deleteSpot,
  getPendingSync,
  getServerVersion,
  markSynced,
  recordServerVersion,
  upsertFromServer,
} from "../db/spotDao";
import type { OutboxOp, Review, Spot, SpotEdit } from "../domain/models";
import type { LiveEvent, SpotPage } from "../domain/schemas";
import { resolveConflict } from "../domain/syncConflict";
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

export function applyLiveEvent(event: LiveEvent) {
  if (event.type === "spot.deleted") deleteIfNotPending(event.id);
  else upsertFromServer([event.spot]);
}

/** Optimistic: the UI shows the edit immediately; the outbox replays it when the server is reachable. */
export function editSpot(id: string, edit: SpotEdit) {
  const now = Date.now();
  getDb().withTransactionSync(() => {
    applyLocalEdit(id, edit, now);
    outboxDao.enqueue({
      opId: randomUUID(),
      type: "update",
      entityId: id,
      payload: { ...edit, editedAt: now },
      createdAt: now,
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
  const serverCopies = new Map<string, Spot>();

  for (const op of outboxDao.allInOrder()) {
    const result = await replay(op);
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

async function replay(op: OutboxOp): Promise<ReplayResult> {
  const { editedAt, ...edit } = op.payload;
  const base = getServerVersion(op.entityId) ?? 0;
  try {
    return synced(op.entityId, await patchSpot(op.entityId, { ...edit, updatedAt: base }, op.opId));
  } catch (e) {
    const serverCopy = conflictServerCopy(e);
    if (serverCopy) return resolve409(op, editedAt, serverCopy);
    if (e instanceof ApiError && e.kind === "http" && e.status !== undefined && e.status < 500) {
      console.warn(`Dropping outbox op ${op.opId}: ${e.message}`);
      return { kind: "dropped" };
    }
    return { kind: "retryLater" };
  }
}

async function resolve409(op: OutboxOp, editedAt: number, serverCopy: Spot): Promise<ReplayResult> {
  if (resolveConflict(editedAt, serverCopy.updatedAt) === "server") {
    return synced(op.entityId, serverCopy);
  }
  const { editedAt: _, ...edit } = op.payload;
  try {
    // The 409 for op.opId is cached by the server, so the re-based write needs a fresh key.
    return synced(
      op.entityId,
      await patchSpot(op.entityId, { ...edit, updatedAt: serverCopy.updatedAt }, randomUUID()),
    );
  } catch {
    return { kind: "retryLater" };
  }
}

function synced(id: string, spot: Spot): ReplayResult {
  recordServerVersion(id, spot.updatedAt);
  return { kind: "synced", spot };
}
