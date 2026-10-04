# UniEats Server

Node 20 + Express server for the UniEats app. At this tag it is read-only: a health check and
the spot list/detail served from an in-memory seed (8 spots, ids `spot-1` … `spot-8`).

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
| GET | `/api/spots/:id` | `Spot` or 404 `{ error: { code, message } }` |

```bash
curl -s 'http://localhost:3000/api/spots?q=pizza' | jq .
curl -s http://localhost:3000/api/spots/spot-3 | jq .
```

## Chaos headers

| Header | Effect |
|---|---|
| `X-Chaos-Delay: 2000` | delay the response by 2000 ms |
| `X-Chaos-Status: 500` | answer with that status |
| `X-Chaos-Malformed: 1` | truncated, invalid JSON |
| `X-Chaos-Drop: 1` | close the socket mid-response |
