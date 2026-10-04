# UniEats Server

Node 20 + Express REST server for the UniEats app, implementing [CONTRACT.md](CONTRACT.md).
In-memory store seeded with 25 spots (`spot-1` … `spot-25`; the first 8 are the ones the
clients hard-coded in l02–l04) and a few reviews. Restarting the server resets the data.

Reads are public; every mutation needs `Authorization: Bearer <jwt>` from `POST /api/auth/login`.
Demo account: `student@unieats.app` / `password` (stored as a bcrypt hash).

## Run

```bash
npm ci
npm start        # http://localhost:3000
npm test         # Jest + supertest, no running server needed
```

Environment: `PORT` (default 3000), `JWT_SECRET` (default `dev-secret-change-me`; set your own
outside the classroom), `ANTHROPIC_API_KEY` (optional, see below), `AI_FALLBACK_CHUNK_MS`
(pause between template chunks, default 150).

## Endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | `/api/health` | – | `{ ok: true }` |
| POST | `/api/auth/login` | – | `{ email, password }` → `{ token, user }` |
| GET | `/api/spots` | – | `?page=1&limit=20&q=&category=` → `{ spots, page, hasNextPage }` |
| GET | `/api/spots/:id` | – | `Spot` or 404 |
| POST | `/api/spots` | Bearer | create, 201 |
| PATCH | `/api/spots/:id` | Bearer | partial update, 200 (409 if based on a stale `updatedAt`) |
| DELETE | `/api/spots/:id` | Bearer | 204 |
| GET | `/api/spots/:id/reviews` | – | `Review[]` |
| POST | `/api/spots/:id/reviews` | Bearer | `{ stars, text }`, 201 |
| GET | `/api/config` | – | `{ flags: { show_new_rating_ui } }` (remote config, default `false`) |
| POST | `/api/config` | Bearer | `{ flags: { show_new_rating_ui: true } }` flips it for the demo |
| POST | `/api/ai/describe` | Bearer | `{ spot: Spot }` → `text/event-stream`, 10 requests/min per user |

Errors are always `{ error: { code, message } }`. Mutations honour `Idempotency-Key`.

```bash
curl -s http://localhost:3000/api/spots | jq '.spots | length, .[0]'
curl -s 'http://localhost:3000/api/spots?page=2' | jq '.hasNextPage'
curl -s http://localhost:3000/api/spots/spot-1/reviews | jq .

TOKEN=$(curl -s -X POST http://localhost:3000/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@unieats.app","password":"password"}' | jq -r .token)

curl -s -X POST http://localhost:3000/api/spots \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-1' \
  -d '{"name":"New Spot","category":"cafe","rating":4.2,"priceLevel":2,"lat":44.427,"lng":26.103,"openNow":true}' | jq .

curl -s -X PATCH http://localhost:3000/api/spots/spot-1 -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"openNow":false}' | jq .

curl -s -X POST http://localhost:3000/api/spots/spot-1/reviews -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"stars":5,"text":"Great soup"}' | jq .
```

## Remote config demo (L11)

```bash
curl -s http://localhost:3000/api/config | jq .        # show_new_rating_ui: false
curl -s -X POST http://localhost:3000/api/config -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"flags":{"show_new_rating_ui":true}}' | jq .
```

Then tap "Fetch & activate" in any client: the new rating badge appears without a rebuild.

## "Describe this dish" (L13)

The app never holds an LLM key: it calls this endpoint and the server talks to Claude.
With `ANTHROPIC_API_KEY` set, the server streams `claude-sonnet-5-5` output through the
Anthropic Messages API; without it, a deterministic template is streamed in 4 chunks so the demo
works offline. Same framing either way:

```
data: {"delta":"Casa Cafea is a cosy place "}

data: {"delta":"to linger over coffee, rated 4.5/5 "}

…
event: done
data: {"source":"template"}
```

```bash
curl -N -X POST http://localhost:3000/api/ai/describe -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"spot":{"name":"Casa Cafea","category":"cafe","priceLevel":2,"rating":4.5,"openNow":true}}'

ANTHROPIC_API_KEY=sk-ant-... npm start      # live model instead of the template
```

The model call lives in `src/routes/ai.js` (`streamFromClaude`).

## Live updates (WebSocket)

`ws://localhost:3000/live` (not `/api/live`) receives one JSON message per change:
`{ "type": "spot.created" | "spot.updated", "spot": Spot }` or `{ "type": "spot.deleted", "id": "…" }`.

```bash
npx wscat -c ws://localhost:3000/live          # terminal 1
curl -s -X PATCH http://localhost:3000/api/spots/spot-1 -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"openNow":false}'   # terminal 2
```

## Chaos headers

| Header | Effect |
|---|---|
| `X-Chaos-Delay: 2000` | delay the response by 2000 ms |
| `X-Chaos-Status: 500` | answer with that status (the request still runs) |
| `X-Chaos-Malformed: 1` | truncated, invalid JSON |
| `X-Chaos-Drop: 1` | close the socket mid-response |

```bash
curl -i http://localhost:3000/api/spots -H 'X-Chaos-Status: 503'
```

## Tests

`npm test` runs everything in-process (supertest; a real socket only for WebSocket and
streaming checks), no running server or API key needed:

| File | Covers |
|---|---|
| `tests/api.test.js` | spots list/search/pagination, CRUD, validation, reviews, idempotency, 409 rule, chaos |
| `tests/auth.test.js` | login, JWT claims, every bad-token case, protected routes |
| `tests/live.test.js` | `/live` broadcasts with a `ws` client |
| `tests/config.test.js` | remote-config flag |
| `tests/ai.test.js` | SSE framing of the offline template, rate limit |
| `tests/ai-claude.test.js` | the Claude streaming path with the SDK mocked |
| `tests/contract.test.js` | black-box conformance to [CONTRACT.md](CONTRACT.md) |

```bash
npx jest tests/contract.test.js      # just the contract suite
```

## CI

`ci/` holds the GitHub Actions workflows for the whole UniEats folder (`server.yml`,
`native.yml`, `cross.yml`). They belong in `.github/workflows/` at the root of the course
repo, where UniEats lives under `unieats/`. Maestro jobs run only on a manual
`workflow_dispatch` with `run_maestro` ticked.
