# UniEats Android — tag `l06-async`

The list and the detail screen come from the UniEats server over HTTP (Ktor + kotlinx
serialization), and the list follows server changes live over a WebSocket. Search, category filter and favourites still work; search and filter are sent to
the server as `q` and `category`.

## Run

```bash
cd unieats/server && npm install && npm start      # http://localhost:3000
./gradlew -p unieats/android :app:installDebug     # emulator reaches the laptop as 10.0.2.2
```

On a physical phone: `adb reverse tcp:3000 tcp:3000` and install with
`./gradlew -p unieats/android :app:installDebug -Punieats.serverHost=localhost:3000`.

## What to look at

- `di/AppModule.kt` — one `HttpClient(OkHttp)` with `HttpTimeout` (connect 10 s, request 15 s),
  `HttpRequestRetry` (3 tries, exponential back-off), JSON content negotiation and the base URL
  `http://10.0.2.2:3000/api/` (`BuildConfig.API_BASE_URL`).
- `data/remote/ApiClient.kt` — `GET /spots?page&limit&q&category`, `GET /spots/{id}`,
  `GET /spots/{id}/reviews`. The list endpoint returns a wrapper (`SpotsPage`), not a bare array.
- `data/remote/ApiResult.kt` + `data/repository/SpotRepository.kt` — every call returns a
  `Result`; failures become a readable message (no swallowed exceptions).
- `ui/spotlist/SpotListViewModel.kt` — explicit `isLoading` / `errorMessage` / empty states,
  `refresh()` for pull-to-refresh, `loadNextPage()` with `currentPage` + `hasNextPage`.
- `ui/spotlist/SpotListScreen.kt` — `PullToRefreshBox`, a full-screen error with **Retry**, a
  snackbar with Retry when a refresh or next page fails, and infinite scroll that stops when the
  server says `hasNextPage: false` (25 seeded spots = page 1 with 20, page 2 with 5).
- Photos come from each spot's `photoUrl` (Coil).
- The detail screen loads the spot and its reviews (read-only — adding a review needs login,
  which arrives at `l08-auth`).

## Live updates (new in l06)

- `data/remote/LiveUpdates.kt` — `events()` is a cold `Flow<LiveEvent>` over the server's
  WebSocket `ws://10.0.2.2:3000/live` (`BuildConfig.LIVE_URL`). It parses
  `spot.created / spot.updated / spot.deleted` and reconnects with exponential back-off
  (1 s → 30 s) when the connection drops.
- `ui/spotlist/SpotListViewModel.kt` — the socket is part of the upstream of
  `uiState = combine(_uiState, liveConnection).stateIn(viewModelScope, WhileSubscribed(5_000), …)`.
  `collectAsStateWithLifecycle()` stops collecting when the screen is not visible, so 5 s later the
  socket closes; it reopens when you come back. A rotation (< 5 s) keeps it open.
- The top bar shows **● Live** while connected.

Demo: keep the list open and change a spot from a terminal — the row updates without a refresh:

```bash
curl -X PATCH localhost:3000/api/spots/spot-3 -H 'Content-Type: application/json' -d '{"name":"Pizza Stop (new oven)"}'
```

Press Home, wait 5 s, and the server log shows the socket closing.

## Try the failure paths

- Stop the server and pull to refresh / relaunch: after 3 tries you get the error state + Retry.
- The server's chaos headers (`X-Chaos-Delay`, `X-Chaos-Status`, `X-Chaos-Malformed`,
  `X-Chaos-Drop`) all end in a message, never a crash.
- Use App Inspection → Network Inspector to watch the calls.
