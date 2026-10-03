# UniEats — one app, four stacks, grown lecture by lecture

A single campus food-spots app implemented four ways (Android, iOS, Flutter, React Native)
against one shared server and one shared [CONTRACT.md](CONTRACT.md). Each lecture advances the
app by one topic, captured as a **git tag** in this repository (`github.com/dancojocar/ma`).
In lecture we `git diff` the previous tag against the current one so students see exactly what
each topic *adds*.

```
unieats/
  server/    Node/Express REST + JWT + WebSocket + SSE + chaos mode  (grows per tag, see below)
  android/   Kotlin · Compose · Hilt · Ktor · Room · WorkManager · KMP shared module
  ios/       Swift · SwiftUI · SwiftData · URLSession            (needs a Mac; CI-built otherwise)
  flutter/   Dart · Riverpod · Drift · dio · go_router           (Android + iOS shells included)
  rn/        Expo SDK 52 · TypeScript · expo-router · Zustand · TanStack Query · expo-sqlite
  maestro/   runner for the per-stack end-to-end flows
```

Application ids: `com.unieats.app` (Android, iOS), `com.unieats.flutter`, `com.unieats.rn` — all
four install side by side on one phone.

## Tag matrix — lecture → tag → what it adds (every stack, unless stated)

| Lecture | Tag | What the tag adds | Server at this tag |
|---|---|---|---|
| L01 Intro | `l01-hello` | One hello screen per stack. The thing students install in Lab 1. | `GET /api/health`, read-only `/api/spots` (8 seed spots), chaos headers |
| L02 Declarative UI | `l02-ui` | Static list + detail from the 8 hard-coded canonical spots; keyed list items. | unchanged |
| L03 State | `l03-state` | ViewModel / store owns search, category filter and favourites; a stateless search bar. | unchanged |
| L04 Navigation | `l04-nav` | List → detail by id, deep links `unieats://spots/<id>` and `https://unieats.app/spots/<id>`. | unchanged |
| L05 Networking | `l05-rest` | List/detail/reviews from the server with loading, error + Retry, empty, pull-to-refresh, pagination on `hasNextPage`, photos. Reviews read-only. | 25-spot seed, full REST (mutations open, no auth yet), `Idempotency-Key` |
| L06 Concurrency | `l06-async` | Live updates over `ws://<host>:3000/live` with reconnect; connection lives only while the list is visible. | `/live` WebSocket broadcast |
| L07 Persistence/Offline | `l07-offline` | Local DB is the single source of truth; "Edit spot" writes optimistically into an outbox; replay on reconnect with `Idempotency-Key`; 409 → last-write-wins; "N changes pending" indicator. | `PATCH` returns 409 on a stale `updatedAt` |
| L08 Security | `l08-auth` | Login, JWT in secure storage, `Authorization` on every mutation and on replay, 401 → login, logout. **Add review** appears here. | `POST /api/auth/login` (bcrypt), JWT required on mutations |
| L09 Background/Sensors | `l09-push-location` | "Spots near me" (2 km) with proper permission flow, updates stopped on leave; a local notification on live `spot.updated`. | unchanged |
| L10 Animations/Perf | `l10-polish` | Android: `animateItem`, expandable About card, NavHost transitions. iOS: list animation + zoom photo transition. Flutter: `Hero` + staggered entrance. RN: `FadeInDown` + swipe gesture. `PERFORMANCE.md` per stack. | unchanged |
| L11 Cloud/Distribution | `l11-cloud` | `show_new_rating_ui` flag from `GET /api/config` with Fetch & activate; consent-gated `CrashReporter` with a debug Test-crash button. No Firebase SDK (see the legacy demos). | `GET/POST /api/config` |
| L12 Architecture | `l12-kmp` | Android: real Kotlin Multiplatform `shared/` module (models + `SyncConflictResolver`, `UniEatsShared` XCFramework) and an `EatsRepository` interface with a fake for tests. iOS links the local `UniEatsDomain` Swift package. Flutter: pure-Dart `packages/unieats_data`. RN: `@unieats/shared` workspace package. | unchanged |
| L13 AI | `l13-ai` | "Describe this dish" streamed token by token over Server-Sent Events from the backend proxy. | `POST /api/ai/describe` (SSE; Claude when `ANTHROPIC_API_KEY` is set, template otherwise) |
| L14 Testing/Interview | `l14-tests` | Unit tests in every stack + one Maestro flow per stack. | contract conformance suite (219 tests) |
| — | branch tip | `unieats/maestro/run.sh` + `.github/workflows/{native,cross,server}.yml` at the repo root (`working-directory: unieats/<stack>`). | — |

Each tag *adds*: nothing from an earlier tag is removed (favourites, search, live updates stay).

## How to use in lecture

```bash
git diff l05-rest l06-async -- unieats/android/   # what L06 added to the Android app
git switch --detach l07-offline                    # check out a lecture's state (whole repo)
cd unieats/server && npm ci && npm start           # the server for that tag, port 3000
```

Run commands per stack (from `unieats/`):

```bash
./android/gradlew -p android :app:installDebug          # Android emulator or device
cd flutter && flutter run                               # Android or iOS
cd rn && npx expo run:android                           # Expo Go cannot open SDK 52 projects
open ios/UniEats.xcodeproj                              # Run on the iOS simulator
```

The Android emulator reaches the server at `10.0.2.2`; Flutter and RN pick the host by platform
and accept an override (`--dart-define=API_BASE_URL=…`, `EXPO_PUBLIC_API_URL=…`). Seed spots are
near 44.427 N, 26.103 E (Bucharest); set the emulator location there for the L09 Nearby screen
(`adb emu geo fix 26.103 44.427`).

## Verification (2026-10-03, every tag from a clean checkout)

Test gates run where tests exist: client unit tests arrive at `l14-tests` (the RN `test` script too); before that the gate is build + analyze.

| Stack | Gate | Result |
|---|---|---|
| server | `npm ci && npm test` | pass at all 14 tags (21 → 219 tests) |
| android | `:app:assembleDebug`, `:app:testDebugUnitTest`, `:shared:allTests` (l12+) | pass; 0 Kotlin warnings; 25 tests at l14 (22 app + 3 shared, the shared ones on 3 targets) |
| flutter | `flutter analyze` (0 issues), `flutter test`, `flutter build apk --debug` | pass; 26 tests at l14 |
| rn | `npm ci && npx tsc --noEmit && npm test && npx expo export` | pass; 21 tests at l14 |
| ios | `xcodebuild … build` (0 warnings), `UniEatsTests` at l14 | pass; 11 tests at l14 |
| maestro | `maestro check-syntax` on all four flows; iOS and Flutter flows executed on a simulator | pass |

Known limits, stated honestly:
- No Android emulator was available during the rebuild: Android and RN flows and deep links were
  verified statically (merged manifest) and against the server from JVM/Node, not on a device.
- `https://unieats.app/...` links need a hosted assetlinks/AASA file to open the app directly; the
  custom scheme works everywhere. Flutter on iOS declares only the custom scheme (no associated domain).
- At `l08` the Android/iOS READMEs and at `l08`–`l09` the Flutter README show a conflict-demo `curl -X PATCH`
  without the Bearer token the server requires from `l08`; prefix it with the login snippet from `server/README.md`.
- RN at `l07`–`l09` resolves a 409 by re-sending a newer local edit on top of the server copy; from
  `l10` it follows the contract exactly (server copy wins, op dropped).
- iOS `l07`–`l13`: a simulator that still has an app store from the pre-rebuild code crashes at
  launch; delete the app once. Fixed at `l14`.
- The Claude path of `/api/ai/describe` was tested with the SDK mocked; the template fallback is
  what runs without a key.
