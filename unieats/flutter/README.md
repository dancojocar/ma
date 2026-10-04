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

Since `l07-offline` a local Drift database is the single source of truth (`lib/data/database/`):
screens only watch Drift; refreshes and live events upsert into it without touching rows with unsynced
edits. "Edit spot" queues an outbox op that `SpotRepository.syncOutbox()` replays in order with
`Idempotency-Key` on reconnect (`connectivity_plus`, behind `connectivityProvider`), when the WebSocket
reconnects, at app start, or from "Sync now". A 409 resolves last-write-wins on `updatedAt`
(`SyncConflictResolver`, in `packages/unieats_data` since l12): a winning local edit is resent from its
place in the queue with a new `Idempotency-Key`; if the server wins, every queued edit of that spot is
dropped. An edit made while its spot is syncing stays queued and is sent next. `liveSyncProvider` writes
live events into Drift; every screen that shows spots watches it (list and detail; Nearby since l09),
because Riverpod 3 pauses the list's listeners while another screen covers it.

Since `l08-auth` the app requires sign-in. `lib/app_router.dart` has one `MaterialApp.router` whose
`authRedirect` sends every location to a gate screen until the session allows it: `/splash` while the
stored session loads, `/unlock` while it is locked (below), `/login` without one (demo user
`student@unieats.app` / `password`). Each gate keeps the original destination in `?from=`, so a deep link
opened while signed out or locked lands on its spot afterwards. `AuthNotifier` in `providers.dart` holds
a sealed `Session` (`SignedOut`, `Locked`, `SignedIn`, a shared model since l12);
`signedInUserProvider` exposes the user.

- `lib/data/auth/token_store.dart` — JWT and user in `flutter_secure_storage` (Android: AES-GCM with a
  Keystore-wrapped key; iOS Keychain "after first unlock, this device only").
- `lib/data/auth/auth_repository.dart` — `POST /auth/login`, restores the session at start-up (the JWT's
  `exp` is checked locally), sign-out.
- `lib/data/auth/auth_interceptor.dart` — `Authorization: Bearer` on every mutation, outbox replay
  included. A 401 to a request that carried a token ends the session: the login screen says "Your
  session expired".
- `lib/ui/login/login_screen.dart` — the sign-in form. "Sign out" is in Settings (since `l11-cloud`).
- "Add review" on the detail screen posts through the outbox (`ReviewRepository.addReview`): the review
  shows at once under a `local:` id ("Waiting to sync") and is swapped for the server's copy on replay.
  `AddReviewController` (like `EditSpotController`) disables the Post button while saving and shows the
  error if the review can't be stored.

Queued edits belong to a user. Every outbox row stores the `user.id` that `/auth/login` returned (Drift
schema v2 adds `outbox.user_id` and renames the reviews column `body` → `text`). Sign out discards
unsynced changes: the outbox, `local:` reviews and pending spot rows go, and the list refetches after the
next sign-in. An expired session (401) or a biometric change keeps them, and they replay when the same
user signs in again; when a different user signs in, `AuthRepository.login` discards the other user's
ops before storing the new token. A 401 during replay is not permanent, so the op stays queued.

Biometric unlock is opt-in: "Unlock with biometrics" in Settings (since `l11-cloud`), shown only when the device has a
strong biometric enrolled (`local_auth`), off until you turn it on. Turning it on moves the JWT into a
second `flutter_secure_storage` store whose key needs a biometric check, so the token cannot be read
without one: on Android a Keystore AES-GCM key with user authentication (`BIOMETRIC_STRONG`) and
`setInvalidatedByBiometricEnrollment(true)`, opened through `BiometricPrompt` with a `CryptoObject`
(Android 9+); on iOS a Keychain item with `biometryCurrentSet`. On a cold start the app opens "Unlock
UniEats" (`lib/ui/login/unlock_screen.dart`) and prompts; cancelling leaves you there, and "Use password
instead" signs out. A new fingerprint or face invalidates the key: the app signs you out with "Biometrics
changed on this device". Sign out and a 401 delete the protected token too; turning the switch off puts
the token back in the normal store. Files: `token_store.dart`, `auth_repository.dart`,
`unlock_screen.dart`, `android/app/src/main/kotlin/com/unieats/flutter/MainActivity.kt`
(`FlutterFragmentActivity`, which `local_auth` needs), `android/app/src/main/AndroidManifest.xml`
(`USE_BIOMETRIC`) and `ios/Runner/Info.plist` (`NSFaceIDUsageDescription`). Demo: enroll a fingerprint
on the emulator, turn the switch on, restart the app and run `adb -e emu finger touch 1`; on the iOS
Simulator use Features → Face ID → Enrolled, then Matching Face.

Since `l09-push-location`:
- **Spots near me** (`/nearby`, the arrow in the list's app bar,
  `lib/ui/nearby/nearby_spots_screen.dart`): spots within 2 km, nearest first, from `geolocator`
  (`lib/services/location_service.dart`). The screen explains why before the system permission prompt
  (`ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` in the manifest, `NSLocationWhenInUseUsageDescription`
  in `Info.plist`), offers "Open settings" when access is blocked and checks again when the app comes
  back to the foreground. `positionProvider` streams positions every 25 m only while the screen is open
  and the app is in the foreground: closing the screen (autoDispose) or backgrounding the app cancels
  the stream and the GPS updates. On the emulator set a campus location first:
  `adb emu geo fix 26.103 44.427` (longitude first), or Simulator → Features → Location → Custom.
- **Local notifications** "<spot> was updated" (`flutter_local_notifications`,
  `lib/services/notification_service.dart`) fired by the live WebSocket's `spot.updated` event in
  `liveSyncProvider`, your own edits included; no FCM/APNs. The permission is asked once, when the first
  signed-in screen opens (`POST_NOTIFICATIONS` on Android 13+, alert/badge/sound on iOS);
  `ios/Runner/AppDelegate.swift` sets the notification-center delegate so they also show while the app
  is in the foreground. The plugin needs core library desugaring (`android/app/build.gradle.kts`). Try:
  `curl -X PATCH …/api/spots/spot-3` with a Bearer token (see the server README).

Since `l10-polish`: the spot photo flies from the list card to the detail header
(`Hero(tag: 'spot-photo-<id>')`; it animates because go_router builds a `MaterialPage` for each route) and
cards fade/slide in, the first ten 40 ms apart (`AnimatedEntrance` in
`lib/ui/spot_list/spot_list_screen.dart`, an `AnimationController` per card, disposed with it, skipped
under "reduce motion"). The Live dot watches `liveEventsProvider.select(...)`, so a live event alone does
not rebuild the list screen; the list changes when the event reaches Drift. Profiling steps:
[PERFORMANCE.md](PERFORMANCE.md).

Since `l11-cloud` there is a Settings screen (`lib/ui/settings/settings_screen.dart`, `/settings`, the gear
in the list's app bar). The list's ⋮ menu is gone: "Unlock with biometrics" and "Sign out" moved to the
Settings Account section. Settings is pushed over the list, so after signing out there the login screen
keeps `?from=/spots` and the next sign-in lands on the list.
- **Remote config** — one flag, `show_new_rating_ui` (default `false`), held by `RemoteConfigNotifier` in
  `providers.dart`. "Fetch & activate" calls `GET /api/config` and applies it at once: the rating on the
  list cards and the detail screen turns into the gradient "★ 4.5 / 5" badge (`NewRatingBadge` in
  `lib/ui/common/spot_badges.dart`). The fetched value lives in memory, so a restart shows the default
  until the next fetch. Flip it on the server with `POST /api/config` (Bearer token)
  `{"flags":{"show_new_rating_ui":true}}`.
- **Crash reporting** — `CrashReporter` interface (`lib/services/crash_reporter.dart`); the default
  logs to the `crash` log channel, behind a consent gate (opt-in switch "Share crash reports", stored
  with `shared_preferences`; every error checks it). `main` wires `FlutterError.onError`,
  `PlatformDispatcher.instance.onError` and a `runZonedGuarded` zone (with `ensureInitialized` inside it),
  and hands the same reporter to Riverpod with `crashReporterProvider.overrideWithValue` (next to
  `retry: (_, _) => null`). Debug builds show a "Test crash" button that calls `recordError()`; nothing
  is reported until you opt in. No Firebase dependency.

This tag (`l12-kmp`) splits out a shared domain layer, the Dart counterpart of the Kotlin
Multiplatform module: `packages/unieats_data` is **pure Dart** (no Flutter, Drift or dio) and holds
the models (`Spot`, `Review`, `User`, the sealed `Session` and the auth exceptions), the live-event
types, `SyncConflictResolver` and the repository interfaces:
- `SpotRepository` — watch/refresh spots, `applyLiveEvent`, `editSpot` (with the `userId` that queues
  it), `syncOutbox`, and the two outbox rules of sign-out: `discardLocalChanges` and
  `discardChangesNotBy`.
- `ReviewRepository` — watch/refresh reviews and the queued `addReview`.
- `AuthRepository` — `login`, `logout` (discards unsynced changes), `endSession` (keeps them),
  `restoreSession`, `unlock` and the biometric switch.

The app implements them (`DriftSpotRepository`, `DriftReviewRepository`, `TokenAuthRepository`), the
providers expose only the interfaces, and the app depends on the package by path
(`unieats_data: path: packages/unieats_data`). `refreshPage` no longer takes dio's `CancelToken`, so the
interface stays free of dio: a search replaced within the 300 ms debounce still sends no request, and a
request already in flight finishes but Riverpod ignores its result. Everything that needs Flutter, a
plugin or Riverpod stays in the app: Drift, dio, `TokenStore`, `LocationService`,
`NotificationService`, `RemoteFlags` and `CrashReporter`. The package keeps no lock file
(`packages/unieats_data/.gitignore`); the app's `pubspec.lock` records it as a path dependency.

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
cd packages/unieats_data && dart pub get && dart analyze   # the shared package on its own
```
