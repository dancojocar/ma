# UniEats iOS — l06-async

Live updates on top of the l05 REST list.

- `Network/LiveUpdateService.swift` — `updates()` returns an `AsyncStream<LiveUpdate>` over a
  `URLSessionWebSocketTask` to `ws://localhost:3000/live`. It pings to confirm the connection,
  decodes `spot.created` / `spot.updated` / `spot.deleted`, reconnects with exponential backoff
  (1, 2, 4 … 30 s) and closes the socket when the consuming task is cancelled.
- `SpotListView` consumes it in `.task(id: scenePhase)`: SwiftUI cancels that task when the
  screen disappears or the app leaves the foreground, which ends the stream and the socket.
  `SpotListViewModel.apply(_:)` patches the visible list; the toolbar shows Live / Offline.

Demo: with the app open, change a spot from a terminal and watch the row update:

```bash
curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Pizza Stop (new oven)"}' \
  http://localhost:3000/api/spots/spot-3
```

Stop the server → the badge turns Offline; start it again → it reconnects on its own.

## From l05 (unchanged)

- `Network/ApiClient.swift` — `URLSession` with 15 s request / 30 s resource timeouts;
  `GET /api/spots?page&limit&q&category`, `GET /api/spots/:id`, `GET /api/spots/:id/reviews`.
  Non-2xx responses become `ApiError.http`, transport failures `.noConnectivity` / `.timeout`,
  bad JSON `.decodingFailed` (try the chaos headers: nothing crashes).
- `SpotListViewModel` — manual pagination: `currentPage`, `isLoadingMore` (reset in `defer`),
  stops when `hasNextPage == false`; search (debounced 300 ms) and category are sent to the
  server as `q` / `category` and restart at page 1; `errorMessage` drives the error UI.
- `SpotListView` — loading, error (with **Retry**), empty and list states; pull-to-refresh;
  infinite scroll (the last row's `.task` loads the next page); `AsyncImage` from `photoUrl`.
- `SpotDetailView` — loads the spot by id and its reviews (read-only "Reviews (N)" section).
  Adding a review needs login and arrives at l08.

## Run

```bash
cd ../server && npm ci && npm start      # http://localhost:3000, 25 seeded spots → 2 pages
open UniEats.xcodeproj                   # pick an iPhone simulator, Cmd+R
```

The simulator shares the Mac's network, so `http://localhost:3000/api` reaches the server
(`NSAllowsLocalNetworking` permits plain http to localhost). To point at another server set
the scheme environment variable `UNIEATS_API_URL` (e.g. `http://192.168.1.20:3000/api`).

Build from the command line:

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```
