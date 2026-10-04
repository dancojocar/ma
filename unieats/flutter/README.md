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

Since `l07-offline` a local Drift database is the single source of truth (`lib/data/database/`):
screens only watch Drift, refreshes and live events upsert into it without touching rows with unsynced
edits, and "Edit spot" queues an outbox op that `SpotRepository.syncOutbox()` replays with
`Idempotency-Key` on reconnect (409 → last-write-wins, `lib/data/sync/sync_conflict_resolver.dart`).

Since `l08-auth` every screen sits behind `/login` (demo user `student@unieats.app` / `password`);
the JWT is kept in `flutter_secure_storage`, sent as `Authorization: Bearer` on every mutation and
outbox replay, a 401 ends the session, "Sign out" is in the list's app bar and "Add review" posts
through the outbox. Drift schema v2 renamed the reviews column `body` → `text` with a migration.

This tag (`l09-push-location`) adds:
- **Spots near me** (`/nearby`, the arrow in the list's app bar): spots within 2 km, nearest first,
  from `geolocator`. The screen explains why before the system permission prompt, offers "Open
  settings" when access is blocked, and position updates stop as soon as the screen closes
  (`positionProvider` is autoDispose). On the emulator set a campus location first:
  `adb emu geo fix 26.103 44.427` (longitude first), or Simulator → Features → Location → Custom.
- **Local notifications** "<spot> was updated" (`flutter_local_notifications`) fired by the live
  WebSocket's `spot.updated` event — no FCM/APNs. Android 13+ asks for `POST_NOTIFICATIONS` when the
  list first opens. Try: `curl -X PATCH …/api/spots/spot-3` with a Bearer token (see the server README).

Demo: turn on airplane mode, edit a spot (see "1 change pending"), turn it off and watch it sync.
Conflict: edit offline, then `curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Server wins"}'
http://localhost:3000/api/spots/spot-3`, reconnect: the server copy wins.

Edits now need a signed-in session. Drift generates `lib/data/database/database.g.dart` (committed). After changing `tables.dart` or
`database.dart` run `dart run build_runner build --delete-conflicting-outputs`.

## Run

Requires Flutter 3.47.6 or newer. The Android shell uses the Gradle wrapper (9.8.0) and AGP 9.4.1 with
built-in Kotlin, and builds with Android Studio 2026.1's bundled JDK 25 as is (JDK 21 works too), so there is
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
