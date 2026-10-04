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
outside the classroom).

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

## CI

`ci/` holds the GitHub Actions workflows for the whole UniEats folder (`server.yml`,
`native.yml`, `cross.yml`). They belong in `.github/workflows/` at the root of the course
repo, where UniEats lives under `unieats/`. Maestro jobs run only on a manual
`workflow_dispatch` with `run_maestro` ticked.
