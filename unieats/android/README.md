# UniEats Android — tag `l05-rest`

The list and the detail screen now come from the UniEats server over HTTP (Ktor + kotlinx
serialization). Search, category filter and favourites still work; search and filter are sent to
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
  `refresh()` for pull-to-refresh, `loadNextPage()` with `currentPage` + `hasNextPage`. Favourites
  still come from `FavouritesRepository`, shared with the detail screen.
- `ui/spotlist/SpotListScreen.kt` — `PullToRefreshBox`, a full-screen error with **Retry**, a
  snackbar with Retry when a refresh or next page fails, and infinite scroll that stops when the
  server says `hasNextPage: false` (25 seeded spots = page 1 with 20, page 2 with 5). The loading,
  error and empty states sit in a one-item `LazyColumn` so they can be pulled to refresh too. When
  the snackbar is dismissed the screen calls `errorShown()`, which clears the message, so the same
  error shows again next time and infinite scroll resumes.
- Photos come from each spot's `photoUrl` (Coil, picsum.photos), so images need internet.
- The detail screen loads the spot and its reviews (read-only — adding a review needs login,
  which arrives at `l08-auth`), with the same loading / error + Retry / not-found states.
- `AndroidManifest.xml` — `INTERNET`, plus `ACCESS_LOCAL_NETWORK`: from Android 17 (API 37)
  apps targeting it need this permission to reach devices on the local network, which includes
  the laptop at `10.0.2.2`. It is only declared here; on API 37 it is also a runtime permission,
  and the request is added at `l09` next to the location prompt. Not verified on an API 37 device
  yet (the API 37 emulator crashes on the course machine); on API 35/36 nothing changes. Loopback
  is not "local network", so `adb reverse` + `localhost:3000` (above) is the fallback.

Unchanged from l04: Navigation 3 with the two-pane layout, deep links, Material Symbols vector
drawables via `painterResource`, the TalkBack descriptions on the rating and reviews, and the
light/dark theme.

## Try the failure paths

- Stop the server and pull to refresh / relaunch: after 3 tries you get the error state + Retry.
- The server's chaos headers (`X-Chaos-Delay`, `X-Chaos-Status`, `X-Chaos-Malformed`,
  `X-Chaos-Drop`) all end in a message, never a crash.
- Use App Inspection → Network Inspector to watch the calls.
