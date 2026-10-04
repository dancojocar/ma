# UniEats — Shared Contract (single source of truth for all stacks)

Every implementation (server, android, ios, flutter, rn) MUST match this exactly so the
same app behaves identically and the cross-stack Maestro/test suites pass against any of them.

## Domain model

```
Spot {
  id: string (uuid)            // server-assigned; clients may use a temp "local:<uuid>" before sync
  name: string
  category: "cafe" | "canteen" | "fastfood" | "bakery" | "bar"
  rating: number               // 0.0–5.0, one decimal
  priceLevel: 1 | 2 | 3
  lat: number
  lng: number
  openNow: boolean
  photoUrl: string             // may be ""
  description: string
  updatedAt: number            // epoch milliseconds — used for last-write-wins sync
}

Review {
  id: string
  spotId: string
  author: string
  stars: 1..5
  text: string
  createdAt: number            // epoch ms
}

User { id: string, email: string, displayName: string }
```

## REST API — base path `/api`, JSON only

| Method | Path | Auth | Body / Query | Response |
|---|---|---|---|---|
| POST | `/auth/login` | no | `{ email, password }` | `{ token, user }` (200) |
| GET | `/spots` | no | `?page=1&limit=20&q=&category=` | `{ spots: Spot[], page, hasNextPage }` |
| GET | `/spots/:id` | no | — | `Spot` (200) / 404 |
| POST | `/spots` | yes | `Spot` (no id) + header `Idempotency-Key` | `Spot` (201) |
| PATCH | `/spots/:id` | yes | partial `Spot` + `Idempotency-Key` | `Spot` (200) |
| DELETE | `/spots/:id` | yes | `Idempotency-Key` | 204 |
| GET | `/spots/:id/reviews` | no | — | `Review[]` |
| POST | `/spots/:id/reviews` | yes | `{ stars, text }` | `Review` (201) |
| GET | `/health` | no | — | `{ ok: true }` |

- **Pagination:** `page` is 1-based; `hasNextPage` is `false` when the last page is returned
  (an empty `spots` array means no more results).
- **Auth:** `Authorization: Bearer <jwt>`. Missing/invalid token on a protected route → 401
  `{ error: { code: "unauthorized", message } }`.
- **Idempotency:** mutating requests carry a client-generated `Idempotency-Key` (uuid). The
  server records keys for 24h and returns the original response for a repeated key — this lets
  the offline queue replay safely. Without the header, mutations still work (not idempotent).
- **Errors:** always `{ error: { code, message } }` with the right HTTP status
  (400 validation, 401 auth, 404 not found, 409 conflict, 500 server).

## JWT

HS256, secret from env `JWT_SECRET` (default `dev-secret-change-me`). Payload:
`{ sub: userId, email, iss: "unieats", aud: "unieats-app", iat, exp }`, `exp` = 1h.
Validation = verify signature AND check `iss`, `aud`, `exp` (not just decode).

## Realtime

`GET /live` (WebSocket). Server broadcasts `{ type: "spot.updated" | "spot.created" | "spot.deleted", spot|id }`
whenever a spot changes. Clients use it for "live updates" (L06) but the app works without it.

## Offline-first sync semantics (the lab's hardest requirement — identical in every stack)

1. **Local DB is the single source of truth.** The UI only ever reads from the local store
   (Room / SwiftData / Drift / expo-sqlite), observed reactively.
2. **Reads:** show local data immediately; refresh from `/spots` in the background; upsert by
   `id`; never delete-then-insert (breaks the reactive stream + loses local edits).
3. **Writes while offline:** apply optimistically to the local DB, mark `pendingSync = true`,
   and enqueue an operation `{ opId(uuid), type: create|update|delete, entityId, payload }`
   in an `outbox` table that survives restarts.
4. **On reconnect:** replay the outbox in order, each carrying its `opId` as `Idempotency-Key`;
   on success clear `pendingSync`; on `409` resolve by **last-write-wins on `updatedAt`**.
5. **Conflict:** if server `updatedAt` > local `updatedAt`, server wins; else client wins.

## Chaos mode (for L05 + the networking kata)

The server honours these request headers (and the chaos-proxy injects them) to simulate a
hostile network — clients must degrade gracefully, never crash:
- `X-Chaos-Delay: <ms>` — delay the response.
- `X-Chaos-Status: 500` — return that status instead of the real one.
- `X-Chaos-Malformed: 1` — return truncated/invalid JSON.
- `X-Chaos-Drop: 1` — close the socket mid-response.

## Seed data

8 spots around a campus (mix of categories, some `openNow:false`, varied ratings), 1 demo
user `student@unieats.app` / `password`, a few reviews. Deterministic (fixed ids) so tests
and screenshots are stable.
