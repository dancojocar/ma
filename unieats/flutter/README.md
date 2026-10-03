# UniEats — Flutter

Discover campus food spots. Since `l05-rest` the app loads everything from the UniEats server:
the list pages through `GET /api/spots` (20 per page, infinite scroll until `hasNextPage` is false,
server-side search and category filter), the detail screen fetches `GET /api/spots/:id` and shows its
reviews read-only from `GET /api/spots/:id/reviews`. Loading, error (with Retry), empty states and
pull-to-refresh on both screens; favourites (in memory) are kept.

- `lib/data/network/api_client.dart` — dio with 10 s connect / 15 s receive timeouts and a
  `RetryInterceptor` (3 retries, exponential backoff) for GETs; failures become a sealed `AppError`.
- `lib/providers/providers.dart` — `SpotPagesNotifier` (`AsyncNotifier`) does the manual pagination.

This tag (`l06-async`) adds live updates: `liveEventsProvider` (a `StreamProvider.autoDispose`) listens to
`ws://<host>:3000/live` (`lib/data/network/live_updates.dart`), and `spot.created/updated/deleted`
events patch the visible list in place. A "Live/Offline" dot in the app bar shows the connection; on a
drop it reconnects with backoff (1 s doubling to 30 s). The socket closes when the list screen is
disposed or the app goes to the background, and reopens on resume. Try it:

```bash
curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Pizza Stop (live)"}' \
  http://localhost:3000/api/spots/spot-3
```

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
