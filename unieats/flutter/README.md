# UniEats — Flutter

Discover campus food spots. Since `l05-rest` the app loads everything from the UniEats server:
the list pages through `GET /api/spots` (20 per page, infinite scroll until `hasNextPage` is false,
server-side search and category filter), the detail screen fetches `GET /api/spots/:id` and shows its
reviews read-only from `GET /api/spots/:id/reviews`. Loading, error (with Retry), empty states and
pull-to-refresh on both screens; favourites (in memory) are kept.

- `lib/data/network/api_client.dart` — dio with 10 s connect / 15 s receive timeouts and a
  `RetryInterceptor` (3 retries, exponential backoff) for GETs; failures become a sealed `AppError`.
- `lib/providers/providers.dart` — `SpotPagesNotifier` (`AsyncNotifier`) does the manual pagination.

Since `l06-async` a WebSocket (`ws://<host>:3000/live`) streams spot changes into the app
(`lib/data/network/live_updates.dart`), reconnecting with backoff; a "Live/Offline" dot shows it.

This tag (`l07-offline`) makes a local Drift database the single source of truth
(`lib/data/database/`): screens only watch Drift; network refreshes and live events upsert into it
without touching rows that have unsynced edits. "Edit spot" on the detail screen changes the row at
once and queues an outbox op; `SpotRepository.syncOutbox()` replays it with `Idempotency-Key` when the
device reconnects (`connectivity_plus`), the WebSocket reconnects, the app starts, or you tap
"Sync now". A 409 resolves last-write-wins on `updatedAt` (`lib/data/sync/sync_conflict_resolver.dart`).

Demo: turn on airplane mode, edit a spot (see "1 change pending"), turn it off and watch it sync.
Conflict: edit offline, then `curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Server wins"}'
http://localhost:3000/api/spots/spot-3`, reconnect: the server copy wins.

Drift generates `lib/data/database/database.g.dart` (committed). After changing `tables.dart` or
`database.dart` run `dart run build_runner build --delete-conflicting-outputs`.

## Run

```bash
cd ../server && npm ci && npm start        # http://localhost:3000
flutter pub get
flutter run
```

The app picks `http://10.0.2.2:3000/api` on the Android emulator and `http://localhost:3000/api`
elsewhere (iOS simulator). On a physical device either `adb reverse tcp:3000 tcp:3000` (Android) or
pass your machine's address: `flutter run --dart-define=API_BASE_URL=http://<ip>:3000/api`
(Android only allows clear-text http to `10.0.2.2`/`localhost`, see
`android/app/src/main/res/xml/network_security_config.xml`).

Deep links (from l04) open a spot directly:
`adb shell am start -W -a android.intent.action.VIEW -d "unieats://spots/spot-3" com.unieats.flutter`
(also `https://unieats.app/spots/spot-3`), or `xcrun simctl openurl booted "unieats://spots/spot-3"`.

Android application id and iOS bundle id: `com.unieats.flutter`.

## Check

```bash
flutter analyze
flutter build apk --debug
```
