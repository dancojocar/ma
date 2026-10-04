# UniEats Android — tag `l14-tests`

Offline-first: Room is the only thing the screens read. The network refreshes Room, the
WebSocket writes into Room, and your edits go to Room first and to an outbox second, which a
WorkManager job replays when the device is online.

## Run

```bash
cd unieats/server && npm install && npm start      # http://localhost:3000
./gradlew -p unieats/android :app:installDebug     # emulator reaches the laptop as 10.0.2.2
```

(Phone: `adb reverse tcp:3000 tcp:3000` and add `-Punieats.serverHost=localhost:3000`.)

## What to look at

- `data/local/` — `SpotEntity` (with `pendingSync`), `ReviewEntity`, `OutboxEntity`
  `{seq, opId, type, entityId, payload}`; `SpotDao.getPendingSync()` / `markSynced()`.
- `data/repository/SpotRepository.kt` — `observe…()` flows from Room; `refreshSpots()` upserts by
  id and never overwrites a row with `pendingSync = true`; `editSpot()` updates the row
  optimistically and queues one `update` op per spot; live WebSocket events are written to Room
  (shared by the list and the detail screen, closed when neither is visible).
- `data/sync/` — `ConnectivityObserver` (`ConnectivityManager.registerDefaultNetworkCallback` as a
  Flow), `SyncScheduler` (enqueues the one-shot `SyncWorker` when the network comes back and after
  every edit), `OutboxSync.replay()` (in order, `Idempotency-Key = opId`, 409 → last-write-wins via
  `SyncConflictResolver`: the newer `updatedAt` wins, a tie goes to the client).
- UI: **Edit spot** on the detail screen (name, description, open now), a "Waiting to sync" chip on
  edited spots and an "N changes pending" banner on the list.

## Login and protected writes (new in l08)

- Sign in with `student@unieats.app` / `password`. The server protects every mutation from this
  tag on.
- `data/auth/TokenStore.kt` — the JWT and display name in `EncryptedSharedPreferences`
  (AES-256-GCM values, key in the Android Keystore). `allowBackup="false"` keeps it out of backups.
- `data/remote/ApiClient.kt` — `authorized()` adds `Authorization: Bearer <jwt>` to PATCH and
  review POST (reads stay anonymous); `OutboxSync` replays with the same header.
- `di/AppModule.kt` — a 401 on a request that carried a token clears the session, and
  `ui/navigation/NavGraph.kt` sends you back to the login screen (also when the 1 h token
  expires). Queued edits stay in the outbox and are replayed after the next sign-in.
- **Log out** is in the ⋮ menu of the list; **Add review** is on the detail screen (online only:
  POST needs the server).

## Nearby + notifications (new in l09)

- The 📍 icon on the list opens **Spots near me**: spots within 2 km, nearest first, with the
  distance. `location/LocationService.kt` wraps the Fused Location Provider
  (`requestLocationUpdates` every 5 s) in a `callbackFlow` whose `awaitClose` calls
  `removeLocationUpdates`; `NearbyViewModel` collects it with `WhileSubscribed()`, so location stops
  the moment you leave the screen or background the app.
- Permission UX (`ui/nearby/NearbyScreen.kt`): an explanation and **Allow location** before the
  system dialog, a stronger rationale when `shouldShowRequestPermissionRationale` is true, and
  **Open settings** after a permanent denial.
- Emulator: set the location to the campus, `adb emu geo fix 26.103 44.427` (longitude first) or
  Extended controls → Location → 44.427, 26.103. The default emulator location (California) shows
  "No spots within 2 km".
- Notifications: on Android 13+ the list asks for `POST_NOTIFICATIONS` once. Every
  `spot.updated` message from the l06 WebSocket posts a local notification "<spot> was updated"
  (`notification/SpotNotificationService.kt`); tapping it opens the spot via the
  `unieats://spots/<id>` deep link. Try it with the curl PATCH below while the list is open.
  (Local notifications driven by the WebSocket only; FCM/APNs push is not used.)

## Animations + performance (new in l10)

- List rows keep `Modifier.animateItem()` (⋮ → Sort by rating shows the reorder).
- Detail screen: tap the **About** card — it expands with `animateContentSize()` (spring) to show
  the full description, price level, coordinates and last update.
- Screen changes slide and fade (`NavHost` enter/exit/popEnter/popExit transitions).
- `PERFORMANCE.md` — the profiling steps from the lecture (HWUI bars, Layout Inspector
  recomposition counts, Perfetto, Memory Profiler).

## Remote config + crash reporting (new in l11)

No Firebase dependency: the same ideas against the course server.

- `cloud/RemoteConfig.kt` — flags from `GET /api/config` with in-app defaults
  (`show_new_rating_ui = false`). ⋮ → **Settings** → **Fetch & activate** applies the server value at
  once; when the flag is true the list shows the new pill-shaped rating badge (`RatingBadge`).
  Flip it for the class (needs a token, see `$TOKEN` below):
  `curl -X POST localhost:3000/api/config -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"flags":{"show_new_rating_ui":true}}'`
- `cloud/CrashReporter.kt` — the app talks to a `CrashReporter` interface; `LogcatCrashReporter`
  logs, `ConsentGatedCrashReporter` drops everything until the user turns on **Share crash
  reports** (persisted, off by default). Uncaught exceptions go through it too
  (`UniEatsApplication`). Debug builds have a **Test crash** button that calls
  `crashReporter.recordError()` — watch Logcat tag `CrashReporter`. Swapping in Crashlytics is a
  new implementation bound in `di/CloudModule.kt`.

## Shared KMP module + testable architecture (new in l12)

- `shared/` is a Kotlin Multiplatform module (`kotlin("multiplatform")`): targets
  `androidTarget()`, `iosArm64()` and `iosSimulatorArm64()`. `commonMain` holds the domain models
  (`Spot`, `Review`, `Category`, request/response types) and `SyncConflictResolver` (server wins only
  when its `updatedAt` is strictly newer; a tie goes to the client). The app depends on `:shared`.
- Tests that run on the JVM **and** on the iOS simulator:
  `./gradlew :shared:allTests` (`shared/src/commonTest`).
- iOS framework: `./gradlew :shared:assembleUniEatsSharedXCFramework` →
  `shared/build/XCFrameworks/{debug,release}/UniEatsShared.xcframework` (needs Xcode on macOS).
- `data/repository/EatsRepository.kt` — the interface the ViewModels use;
  `DefaultEatsRepository` (Room + Ktor + WebSocket) is bound to it in `di/AppModule.kt`.
- Unit tests without Android: `./gradlew :app:testDebugUnitTest` runs `SpotListViewModelTest`
  against `FakeEatsRepository` and `FakeFeatureFlags` (`app/src/test`).

## "Describe this dish" (new in l13)

- Detail screen → **Describe this dish**. The app sends the full spot (`openNow` included) to
  `POST /api/ai/describe` with the JWT; the server calls Claude when it has `ANTHROPIC_API_KEY`
  and otherwise streams a template, so the demo works offline. The app never holds an API key.
- `data/remote/AiDescribeClient.kt` — Ktor `preparePost(...).execute { bodyAsChannel() }` reads the
  `text/event-stream` line by line; `ServerSentEventParser` (`data/remote/ServerSentEvents.kt`,
  unit-tested in `app/src/test`) turns lines into events; each `data: {"delta": …}` is emitted
  into a `Flow<String>`; `event: done` ends it, `event: error` throws. Retries are off and the
  timeout is longer for this one request.
- `SpotDetailViewModel.describeDish()` appends every delta to `aiText`, so the text grows on screen
  as it arrives (`animateContentSize`). A stopped server shows "Server unreachable".

## Tests (new in l14)

```bash
./gradlew -p unieats/android :app:testDebugUnitTest   # JUnit 4 + MockK + Turbine, no device
./gradlew -p unieats/android :shared:allTests          # commonTest on the JVM and the iOS simulator
```

- `SpotListViewModelTest` — ViewModel against `FakeEatsRepository` (first load, debounced search,
  category filter, favourites, error + retry, a repository change observed with Turbine, remote flag).
- `SettingsViewModelTest` — MockK mocks for `RemoteConfig`, `CrashConsent` and `CrashReporter`,
  Turbine on `uiState`, `verify { crashReporter.recordError(...) }`.
- `EntityMappingTest` (Spot/Review ↔ Room entities), `ServerSentEventParserTest`, `GeoTest`.
- `shared/src/commonTest/.../SyncConflictResolverTest` — newer server wins, newer client wins,
  tie → client.

### Maestro (UI flow on an emulator)

Start the server, install the debug app, then:

```bash
maestro test unieats/android/maestro/android.yaml
```

The flow signs in, opens the first spot, checks "Reviews (n)", taps **Describe this dish**, waits
for the streamed text and goes back. It finds composables by `testTag`
(`spot-list-item`, `spot-detail-reviews`, `describe-dish-button`, …, see `ui/TestTags.kt`), which
`MainActivity` exposes as resource ids with `testTagsAsResourceId`.
`maestro check-syntax maestro/android.yaml` validates the file without a device.

## Demo: offline edit → reconnect → sync

1. Open a spot, tap **Edit spot**, change the name, Save — the list shows it immediately.
2. Turn on airplane mode (or stop the server), edit again: "1 change pending" stays visible.
3. Turn the network back on (or restart the server): the banner disappears, and the server log shows
   one `PATCH` with the `Idempotency-Key` header.
4. Conflict: edit a spot offline, change the same spot with curl
   (from l08 the server needs a token: `TOKEN=$(curl -s localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"email":"student@unieats.app","password":"password"}' | jq -r .token)`,
   then `curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"name":"Server name"}'`),
   then go online — the server's newer version wins (409 → the row is replaced, the op dropped).

Kill and relaunch the app at any point: cached spots show instantly and the outbox survives.
