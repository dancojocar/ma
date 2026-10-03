# UniEats Server

Node 20 + Express REST server for the UniEats app, implementing [CONTRACT.md](CONTRACT.md).
In-memory store seeded with 25 spots (`spot-1` … `spot-25`; the first 8 are the ones the
clients hard-coded in l02–l04) and a few reviews. Restarting the server resets the data.

At this tag mutations are open (no auth yet): that arrives at l08.

## Run

```bash
npm ci
npm start        # http://localhost:3000
npm test         # Jest + supertest, no running server needed
```

`PORT` changes the port.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | `{ ok: true }` |
| GET | `/api/spots` | `?page=1&limit=20&q=&category=` → `{ spots, page, hasNextPage }` |
| GET | `/api/spots/:id` | `Spot` or 404 |
| POST | `/api/spots` | create, 201 |
| PATCH | `/api/spots/:id` | partial update, 200 |
| DELETE | `/api/spots/:id` | 204 |
| GET | `/api/spots/:id/reviews` | `Review[]` |
| POST | `/api/spots/:id/reviews` | `{ stars, text }`, 201 |

Errors are always `{ error: { code, message } }`. Mutations honour `Idempotency-Key`.

```bash
curl -s http://localhost:3000/api/spots | jq '.spots | length, .[0]'
curl -s 'http://localhost:3000/api/spots?page=2' | jq '.hasNextPage'
curl -s http://localhost:3000/api/spots/spot-1/reviews | jq .

curl -s -X POST http://localhost:3000/api/spots \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-1' \
  -d '{"name":"New Spot","category":"cafe","rating":4.2,"priceLevel":2,"lat":44.427,"lng":26.103,"openNow":true}' | jq .

curl -s -X PATCH http://localhost:3000/api/spots/spot-1 \
  -H 'Content-Type: application/json' -d '{"openNow":false}' | jq .
```

## Live updates (WebSocket)

`ws://localhost:3000/live` (not `/api/live`) receives one JSON message per change:
`{ "type": "spot.created" | "spot.updated", "spot": Spot }` or `{ "type": "spot.deleted", "id": "…" }`.

```bash
npx wscat -c ws://localhost:3000/live          # terminal 1
curl -s -X PATCH http://localhost:3000/api/spots/spot-1 \
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
