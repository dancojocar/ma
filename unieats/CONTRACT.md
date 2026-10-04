# UniEats — Shared Contract (single source of truth for all stacks)

Every implementation (server, android, ios, flutter, rn) MUST match this exactly so the
same app behaves identically and the cross-stack tests pass against any of them.

## Domain model

```
Spot {
  id: string                   // seed: "spot-1" … "spot-25"; server-created: uuid
  name: string
  category: "cafe" | "canteen" | "fastfood" | "bakery" | "bar"
  rating: number               // 0.0–5.0, one decimal
  priceLevel: 1 | 2 | 3
  lat: number
  lng: number
  openNow: boolean
  photoUrl: string             // may be ""
  description: string
  updatedAt: number            // epoch milliseconds, set by the server on every write
}

Review {
  id: string
  spotId: string
  author: string
  stars: 1..5                  // integer
  text: string
  createdAt: number            // epoch ms
}
```

## REST API — base path `/api`, JSON only

| Method | Path | Body / Query | Response |
|---|---|---|---|
| GET | `/health` | — | `{ ok: true }` |
| GET | `/spots` | `?page=1&limit=20&q=&category=` | `{ spots: Spot[], page, hasNextPage }` |
| GET | `/spots/:id` | — | `Spot` (200) / 404 |
| POST | `/spots` | `Spot` without `id`/`updatedAt` (+ `Idempotency-Key`) | `Spot` (201) |
| PATCH | `/spots/:id` | partial `Spot` (+ `Idempotency-Key`) | `Spot` (200) |
| DELETE | `/spots/:id` | (+ `Idempotency-Key`) | 204 |
| GET | `/spots/:id/reviews` | — | `Review[]` |
| POST | `/spots/:id/reviews` | `{ stars, text }` (+ `Idempotency-Key`) | `Review` (201) |

- **Pagination:** `page` is 1-based, default `limit` is 20. `hasNextPage` is `false` on the last
  page; a page past the end returns `spots: []`.
- **Search:** `q` matches name or description, case-insensitive; `category` is an exact match;
  both combine.
- **Writes:** the server assigns `id` and `updatedAt`; unknown fields are ignored; invalid
  values → 400. Deleting a spot deletes its reviews.
- **Idempotency:** mutating requests may carry a client-generated `Idempotency-Key` (uuid). The
  server keeps the response for 24h and replays it (same status and body, header
  `Idempotent-Replayed: true`) for a repeated key. This is what makes outbox replay safe.
  Without the header, mutations still work but are not idempotent.
- **Conflict rule (PATCH):** a PATCH body may carry the `updatedAt` of the version the client
  edited. If it is **older** than the stored `updatedAt` the write is rejected with
  409 `{ error: { code: "conflict", message }, spot: <current server Spot> }` and nothing changes.
  Equal or newer (or absent) → the patch is applied and the server sets a new, strictly larger
  `updatedAt`. A 409 is cached under its `Idempotency-Key` like any other response.
- **Errors:** always `{ error: { code, message } }` with the matching status:
  400 `validation` (also for a body that is not valid JSON), 404 `not_found`, 409 `conflict`,
  500 `server_error`.

## Realtime

`ws://<host>:3000/live` (WebSocket, path `/live`, not under `/api`). After every successful
spot write the server broadcasts one JSON text message to all connected clients:

```
{ "type": "spot.created", "spot": Spot }
{ "type": "spot.updated", "spot": Spot }
{ "type": "spot.deleted", "id": string }
```

An idempotent replay does not broadcast again. Reviews are not broadcast. Clients use it for
live updates (L06) but the app must work without it (reconnect with backoff).

## Offline-first sync semantics (identical in every stack, from L07)

1. **Local DB is the single source of truth.** The UI only ever reads from the local store
   (Room / SwiftData / Drift / expo-sqlite), observed reactively.
2. **Reads:** show local data immediately; refresh from `/spots` in the background; upsert by
   `id`; never delete-then-insert (breaks the reactive stream); never overwrite a row whose
   `pendingSync = true`.
3. **Writes:** apply optimistically to the local DB, set `pendingSync = true`, and enqueue
   `{ opId (uuid), type: create|update|delete, entityId, payload }` in an `outbox` table that
   survives restarts. An update's payload includes the `updatedAt` the user edited.
4. **Replay** (on reconnect): send the outbox in order, each with `Idempotency-Key: <opId>`; on
   2xx store the server's Spot and clear `pendingSync`.
5. **Conflict — last-write-wins on `updatedAt`:** if server `updatedAt` > local `updatedAt`, the
   server wins; otherwise (including a tie) the client wins. The server enforces this with the
   409 rule above, so on a 409 the client replaces its row with `spot` from the response, clears
   `pendingSync` and drops the operation.

## Chaos mode (L05 + the networking kata)

The server honours these request headers on every route to simulate a hostile network; clients
must degrade gracefully, never crash:
- `X-Chaos-Delay: <ms>` — delay the response.
- `X-Chaos-Status: <code>` — answer with that status (the request is still processed).
- `X-Chaos-Malformed: 1` — return truncated/invalid JSON.
- `X-Chaos-Drop: 1` — close the socket mid-response.

## Seed data

Deterministic (fixed ids) so tests and screenshots are stable. Restarting the server resets it.
- **25 spots**, ids `spot-1` … `spot-25`, all within ~1 km of the campus centre
  (44.427 N, 26.103 E, Bucharest), every category represented, some `openNow: false`, varied
  ratings, `photoUrl` = `https://picsum.photos/seed/<id>/400/300`. Page size 20 gives two pages.
- The **first 8 are canonical**: the clients hard-code exactly these in l02–l04.

| id | name | category | rating | priceLevel | openNow |
|---|---|---|---|---|---|
| spot-1 | Central Canteen | canteen | 3.8 | 1 | true |
| spot-2 | Espresso Lab | cafe | 4.5 | 2 | true |
| spot-3 | Pizza Stop | fastfood | 4.1 | 2 | true |
| spot-4 | Bread & Butter | bakery | 4.7 | 1 | false |
| spot-5 | The Pub Garden | bar | 4.2 | 3 | false |
| spot-6 | Sushi Box | fastfood | 3.9 | 2 | true |
| spot-7 | Campus Bistro | cafe | 4.3 | 2 | true |
| spot-8 | Grandma's Kitchen | canteen | 4.6 | 1 | true |

  Coordinates, descriptions and `updatedAt` are in `server/src/seed.js`.
- 4 reviews (two on `spot-1`, one each on `spot-2` and `spot-3`).
