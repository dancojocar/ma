# UniEats — Shared Contract (single source of truth for all stacks)

Every implementation (server, android, ios, flutter, rn) MUST match this exactly so the
same app behaves identically and the cross-stack tests pass against any of them.
`server/tests/contract.test.js` checks the server against this file.

## When each part appears

| Tag | Server surface |
|---|---|
| l01-hello … l04-nav | `GET /health`, `GET /spots`, `GET /spots/:id`, chaos headers (clients still use hard-coded data) |
| l05-rest | 25-spot seed, spot mutations + reviews **without auth**, `Idempotency-Key` |
| l06-async | `ws://<host>:3000/live` |
| l07-offline | PATCH conflict rule (409) |
| l08-auth | `POST /auth/login`, JWT required on every mutation |
| l11-cloud | `GET/POST /config` |
| l13-ai | `POST /ai/describe` streaming SSE |

## Connecting

| Client | REST base URL | WebSocket |
|---|---|---|
| Android emulator (native, Flutter, RN) | `http://10.0.2.2:3000/api` | `ws://10.0.2.2:3000/live` |
| iOS simulator (native, Flutter, RN) | `http://localhost:3000/api` | `ws://localhost:3000/live` |

Flutter and RN choose by platform automatically and accept an override (dart-define / env).

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

User { id: string, email: string, displayName: string }
```

## REST API — base path `/api`, JSON only

| Method | Path | Auth | Body / Query | Response |
|---|---|---|---|---|
| GET | `/health` | no | — | `{ ok: true }` |
| POST | `/auth/login` | no | `{ email, password }` | `{ token, user: User }` (200) |
| GET | `/spots` | no | `?page=1&limit=20&q=&category=` | `{ spots: Spot[], page, hasNextPage }` |
| GET | `/spots/:id` | no | — | `Spot` (200) / 404 |
| POST | `/spots` | yes | `Spot` without `id`/`updatedAt` (+ `Idempotency-Key`) | `Spot` (201) |
| PATCH | `/spots/:id` | yes | partial `Spot`, optional `updatedAt` (+ `Idempotency-Key`) | `Spot` (200) / 409 |
| DELETE | `/spots/:id` | yes | (+ `Idempotency-Key`) | 204 |
| GET | `/spots/:id/reviews` | no | — | `Review[]` |
| POST | `/spots/:id/reviews` | yes | `{ stars, text }` (+ `Idempotency-Key`) | `Review` (201) |
| GET | `/config` | no | — | `{ flags: { show_new_rating_ui: boolean } }` |
| POST | `/config` | yes | `{ flags: { show_new_rating_ui: boolean } }` | `{ flags }` (200) |
| POST | `/ai/describe` | yes | `{ spot: Spot }` (full spot, incl. `openNow`) | `text/event-stream` (200) |

- **Auth:** `Authorization: Bearer <jwt>` on every mutation (from L08; before L08 they are
  open). Missing/invalid/expired token → 401 `{ error: { code: "unauthorized", message } }`
  with `WWW-Authenticate: Bearer`. Clients treat a 401 as "session over" and go back to login.
  A review's `author` is the signed-in user's `displayName`.
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
  400 `validation` (also for a body that is not valid JSON), 401 `unauthorized`, 404 `not_found`,
  409 `conflict`, 429 `rate_limited`, 500 `server_error`.

## JWT

HS256, secret from env `JWT_SECRET` (default `dev-secret-change-me`). Payload:
`{ sub: userId, email, iss: "unieats", aud: "unieats-app", iat, exp }`, `exp` = 1h.
Validation = signature (constant-time compare, algorithm fixed to HS256 so `alg: none` is
rejected) AND `iss`, `aud`, `exp` — never just decode. Passwords are stored as bcrypt hashes.

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

## Remote config (from L11)

`GET /api/config` returns every feature flag with its current value (`Cache-Control: no-store`).
Today there is one flag, `show_new_rating_ui` (default `false`), which toggles the "new rating"
badge in every client. Clients ship the same default, fetch on "Fetch & activate" and apply the
value immediately. `POST /api/config` (auth) changes known flags for the classroom demo; unknown
flags or non-boolean values → 400. Values reset when the server restarts.

## AI describe — Server-Sent Events (from L13)

`POST /api/ai/describe`, auth required, at most 10 requests per minute per user (then 429
`rate_limited` with `Retry-After`). Validation failures (no `spot.name`, invalid JSON) are
ordinary 400 JSON responses. Otherwise the response is `200 text/event-stream`:

```
data: {"delta":"<text>"}            // one or more, append in order

event: done
data: {"source":"template" | "<model id>"}
```

or, if generation fails after the stream started, a final

```
event: error
data: {"error":{"code":"ai_unavailable" | "refused","message":"…"}}
```

Frames are separated by a blank line. With `ANTHROPIC_API_KEY` set on the server, the text
comes from the Anthropic Messages API (`claude-sonnet-5-5`, streamed); without it, a
deterministic template is streamed in exactly 4 `delta` chunks with the same framing, so
clients behave identically offline. A client that cannot reach the server shows a "server
unreachable" message.

## Offline-first sync semantics (identical in every stack, from L07)

1. **Local DB is the single source of truth.** The UI only ever reads from the local store
   (Room / SwiftData / Drift / expo-sqlite), observed reactively.
2. **Reads:** show local data immediately; refresh from `/spots` in the background; upsert by
   `id`; never delete-then-insert (breaks the reactive stream); never overwrite a row whose
   `pendingSync = true`.
3. **Writes:** apply optimistically to the local DB, set `pendingSync = true`, and enqueue
   `{ opId (uuid), type: create|update|delete, entityId, payload }` in an `outbox` table that
   survives restarts. An update's payload includes the `updatedAt` the user edited.
4. **Replay** (on reconnect): send the outbox in order, each with `Idempotency-Key: <opId>`
   and, from L08, the `Authorization` header; on 2xx store the server's Spot and clear
   `pendingSync`.
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

| id | name | category | rating | priceLevel | lat | lng | openNow | description | updatedAt |
|---|---|---|---|---|---|---|---|---|---|
| spot-1 | Central Canteen | canteen | 3.8 | 1 | 44.4268 | 26.1025 | true | The main campus canteen with daily hot meals. | 1700000000000 |
| spot-2 | Espresso Lab | cafe | 4.5 | 2 | 44.4275 | 26.103 | true | Specialty coffee and pastries near the library. | 1700000001000 |
| spot-3 | Pizza Stop | fastfood | 4.1 | 2 | 44.426 | 26.1015 | true | Pizza by the slice for students on the go. | 1700000002000 |
| spot-4 | Bread & Butter | bakery | 4.7 | 1 | 44.428 | 26.104 | false | Fresh bread and pastries baked every morning. | 1700000003000 |
| spot-5 | The Pub Garden | bar | 4.2 | 3 | 44.4255 | 26.101 | false | Outdoor bar with craft beers and snacks. | 1700000004000 |
| spot-6 | Sushi Box | fastfood | 3.9 | 2 | 44.427 | 26.105 | true | Grab-and-go sushi rolls and bento boxes. | 1700000005000 |
| spot-7 | Campus Bistro | cafe | 4.3 | 2 | 44.4265 | 26.1035 | true | Relaxed cafe with sandwiches, salads and wifi. | 1700000006000 |
| spot-8 | Grandma's Kitchen | canteen | 4.6 | 1 | 44.4272 | 26.102 | true | Traditional home-cooked Romanian meals. | 1700000007000 |

`photoUrl` follows the picsum rule above. `spot-9` … `spot-25` are in `server/src/seed.js`.

- 4 reviews (two on `spot-1`, one each on `spot-2` and `spot-3`).
- 1 demo user `student@unieats.app` / `password` (id `user-1`, displayName `Demo Student`).
