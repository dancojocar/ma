# UniEats — Flutter

Discover campus food spots. Since `l05-rest` the app loads everything from the UniEats server:
the list pages through `GET /api/spots` (20 per page, infinite scroll until `hasNextPage` is false,
server-side search and category filter), the detail screen fetches `GET /api/spots/:id` and shows its
reviews read-only from `GET /api/spots/:id/reviews`. Loading, error (with Retry), empty states and
pull-to-refresh on both screens; favourites (in memory) and the go_router navigation from l04 are kept.
Widgets come from the `material_ui` package; state is Riverpod 3 without code generation.

- `lib/data/network/api_client.dart` — dio with 10 s connect / 15 s receive timeouts and a
  `RetryInterceptor` (3 retries, exponential backoff) for GETs; failures become a sealed `AppError`.
  `ProviderScope(retry: (_, _) => null)` turns off Riverpod 3's own provider retry, so retrying
  stays in this one place.
- `lib/providers/providers.dart` — `SpotPagingNotifier` (`AsyncNotifier`, autoDispose) does the manual
  pagination; since `l07-offline` each page is written to the local database instead of held in memory.

Since `l06-async` a WebSocket (`ws://<host>:3000/live`) streams spot changes into the app
(`lib/data/network/live_updates.dart`), reconnecting with backoff; a "Live/Offline" dot shows it. The
socket closes when no visible screen watches it or the app goes to the background.

This tag (`l07-offline`) makes a local Drift database the single source of truth
(`lib/data/database/`): screens only watch Drift; network refreshes and live events upsert into it
without touching rows that have unsynced edits. "Edit spot" on the detail screen changes the row at
once and queues an outbox op; `SpotRepository.syncOutbox()` replays it with `Idempotency-Key` when the
device reconnects (`connectivity_plus`, behind `connectivityProvider`), the WebSocket reconnects, the
app starts, or you tap "Sync now". Ops replay strictly in the order they were made. A 409 resolves
last-write-wins on `updatedAt` (`lib/data/sync/sync_conflict_resolver.dart`): if the local edit wins,
it is resent from its place in the queue with the server's version as base and a new
`Idempotency-Key` (the server remembers the 409 under the old key); if the server wins, every queued
edit of that spot is dropped, since they all built on the version the server has replaced. An edit made
while its spot is syncing stays queued and is sent next. An edit the server rejects for good (another
4xx) is dropped on its own; the screen shows the server copy unless later edits are still queued. The
edit sheet's Save button is disabled while saving (`EditSpotController` in `providers.dart`) and shows the error if the edit can't be stored.

`liveSyncProvider` writes live events into Drift. Both the list and the detail screen watch it, because
Riverpod 3 pauses the list's listeners while the detail screen covers it; with only the list watching,
the detail screen would stop receiving live changes.

Demo: turn on airplane mode, edit a spot (see "1 change pending"), turn it off and watch it sync.
Conflict: edit offline, then `curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Server wins"}'
http://localhost:3000/api/spots/spot-3`, reconnect: the server copy wins.

Drift generates `lib/data/database/database.g.dart` (committed). After changing `tables.dart` or
`database.dart` run `dart run build_runner build --delete-conflicting-outputs`.

## Run

Requires Flutter 3.47.6 or newer (developed on 3.50). The Android shell uses the Gradle wrapper (9.8.0) and
AGP 9.4.1 with built-in Kotlin, and builds with Android Studio 2026.1's bundled JDK 25 as is (JDK 21 works too), so there is
no `flutter config --jdk-dir` step. iOS (tested with Xcode 27, deployment target 15.0) links plugins as Swift
packages: no CocoaPods, no `pod install`.

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
