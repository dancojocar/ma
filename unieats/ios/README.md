# UniEats iOS — l14-tests

Unit tests (Swift Testing) and one Maestro UI flow.

## What this tag adds

- **`UniEatsTests` target + shared scheme** (also wired into the `UniEats` scheme, so ⌘U
  works). Swift Testing, main-actor default isolation like the app. No test touches the network
  or the Keychain, so the suites also pass in a build without code signing (CI):
  - `SyncReplayTests` — the outbox replay against an in-process stub server: an edit made while
    its spot's PATCH is in flight is kept and sent in the same run, rebased on the first
    response's `updatedAt`; edits queued while a sync runs are sent by that run; a 409 takes the
    server copy and drops the op plus the later queued ops of that spot (other spots still sync);
    an unreadable 409 or another 4xx drops the edit and reloads the spot; 404 removes it;
    offline keeps it queued.
  - `SessionOutboxTests` — Log out discards the outbox and reloads the optimistic rows; a 401
    keeps the edit (with its user id) and the same user sends it after logging in again; another
    user logging in discards it unsent.
  - `SessionStoreTests` — the session phases with a `FakeBiometrics` and a throwaway
    `UserDefaults` suite: login/restore, wrong password, a token left by a reinstall, biometric
    unlock on/off, a locked launch that neither prompts nor reads the token, unlock, cancel stays
    locked, a new enrolment or no biometrics signs out with a notice, logout and 401 delete the
    biometric item. The `.biometryCurrentSet` Keychain item itself needs a signed build and
    Face ID; check it by hand (simulator recipe in the `l08-auth` README).
  - `PendingDeepLinkTests` — a link opened on the login or unlock screen is shown once after
    login or unlock; invalid links are ignored; the Spots tab is selected.
  - `ReviewSubmissionTests` — `ReviewDraft` keeps its Idempotency-Key until the stars or the
    text change; a retry after a 503 resends the same key, edited text gets a new one.
  - `AiDescribeClientTests` — SSE parsing: deltas in order, comments and unknown fields
    ignored, CRLF line endings, `event: done` ends the stream, `event: error` throws the
    server's message, 401/429/500; the request carries the full spot and the Bearer token; a
    401 from `SpotRepository.describe` ends the session.
  - `OfflineUpsertTests` — in-memory SwiftData: live updates upsert synced rows but never
    overwrite a `pendingSync` row; two offline edits coalesce into one outbox entry.
  - `SpotDTOMappingTests` — CONTRACT JSON → `SpotDTO` → domain `Spot`; encoded field names;
    outbox PATCH payload shape.
  - `DeepLinkTests` — both link forms accepted, other hosts/paths rejected.
  - Helpers in `UniEatsTests/Support/`: `StubServer` (a `URLProtocol` with one host name per
    test, so suites run in parallel), `SpotBackend` (login, GET, PATCH with the server's 409
    rule), `InMemoryTokenStore`, `FakeBiometrics`, `Gate` (holds a response until the test
    releases it).
- **`UniEatsDomain/Tests`** — `SpotConflictResolverTests` (server newer → server; local newer
  → client; tie → client) run with `swift test`, next to the code they test.
- Seams the tests use: `ApiClient(baseURL:configuration:)`, `SessionStore(api:tokens:defaults:biometrics:)`
  with the `TokenStoring` protocol (`KeychainHelper` in the app), `SpotRepository(…, ai:)`, and
  `Navigation/AppRouter.swift`, which now holds the selected tab and the pending deep link.
- **`maestro/ios.yaml`** — login (dismissing "Save Password?" and the notification prompt when
  they appear) → first spot (`spot-list-item`) → "Reviews (N)" (`spot-detail-reviews`) →
  **Describe this dish** (`describe-dish-button`, `describe-dish-text`) → swipe back.
  Accessibility identifiers were added for exactly these selectors, next to the existing
  accessibility labels. Biometric unlock is off by default, so the flow always meets the login
  screen.
- An incompatible SwiftData store (e.g. left by another checkout of the app) is discarded and
  recreated instead of crashing at launch.

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEatsTests \
  -destination 'platform=iOS Simulator,name=iPhone 17,OS=27.0' test CODE_SIGNING_ALLOWED=NO
swift test --package-path UniEatsDomain
maestro check-syntax maestro/ios.yaml
cd ../server && npm start            # in another terminal; then build with CODE_SIGN_IDENTITY=-
                                     # (the flow logs in, which needs the Keychain), install, and:
maestro test maestro/ios.yaml
```

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay (launch, reconnect, every edit, retry with backoff), edits during a sync sent in the same run, 409 → the server copy wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login (queued edits kept, each records its user), Log out (Settings tab from l11) discards them, add review with one Idempotency-Key per draft, deep link kept while logged out or locked | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift`, `Models/ReviewDraft.swift` |
| l08 | Opt-in biometric unlock (Settings › Account from l11): `.biometryCurrentSet` Keychain item read with an evaluated `LAContext`, "Unlock UniEats" screen, "Use password instead" logs out | `Keychain/BiometricAuth.swift`, `Views/UnlockView.swift` |
| l09 | Tabs (Spots, Near Me) with shared favourites; a deep link selects the Spots tab | `UniEatsApp.swift` (`Navigation/AppRouter.swift` from l14), `ViewModels/FavouritesStore.swift` |
| l09 | Near Me tab (CoreLocation when-in-use, 2 km, updates only while visible), local notification from live `spot.updated` | `Views/NearMeView.swift`, `Location/LocationManager.swift`, `Notifications/SpotChangeNotifier.swift` |
| l09 | Background replay of pending edits (`BGAppRefreshTask` `com.unieats.app.outbox-sync`, scheduled when the app goes to the background with edits queued) | `Repository/SpotRepository.swift`, `UniEatsApp.swift`, `Info.plist` |
| l10 | Animated filtered list, row photo → detail zoom transition, `PERFORMANCE.md` | `Views/SpotListView.swift`, `Views/SpotRow.swift` |
| l10 | String Catalog with plural variants, `String(localized:)` outside views; VoiceOver labels for ratings and prices, `.isSelected` chips, 4.5:1 contrast colours | `Localizable.xcstrings`, `Views/Palette.swift` |
| l11 | Settings tab: `show_new_rating_ui` Fetch & activate (`NewRatingBadge`), consent-gated `CrashReporter` with a debug Test crash; Settings › Account holds the Face ID switch and Log out | `Cloud/`, `Views/SettingsView.swift`, `Views/SpotRow.swift` |
| l12 | Local Swift package `UniEatsDomain` (domain `Spot`, `SpotConflictResolver`, nonisolated `Sendable` types) linked into the app; on a 409 the server copy wins and the stale queued edits of that spot are dropped | `UniEatsDomain/`, `Repository/SpotRepository.swift` |
| l13 | "Describe this dish" streamed over SSE (`URLSession.bytes` + `AsyncThrowingStream`, read off the main actor) with the full spot and the Bearer token; 401 ends the session, 429 "wait a minute"; VoiceOver announces the result; the server streams a template without `ANTHROPIC_API_KEY` | `AI/AiDescribeClient.swift`, `Views/DescribeDishSection.swift` |

The simulator cannot run the l09 background replay: `submit` fails with "not available on this
platform" (logged under `com.unieats.app` / `sync` and ignored). On a device, pause in the
debugger after sending the app to the background and run

```
e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.unieats.app.outbox-sync"]
```

## Run

```bash
cd ../server && npm ci && npm start      # http://localhost:3000
open UniEats.xcodeproj                   # Xcode 26 or later; pick an iPhone simulator, Cmd+R
```

Log in with `student@unieats.app` / `password`. The simulator reaches the Mac's
`http://localhost:3000/api` and `ws://localhost:3000/live`; set the scheme environment variable
`UNIEATS_API_URL` to use another server. Mutations need a token, e.g. for curl demos:

```bash
TOKEN=$(curl -s -H 'Content-Type: application/json' \
  -d '{"email":"student@unieats.app","password":"password"}' localhost:3000/api/auth/login \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')
curl -X PATCH -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Pizza Stop (new oven)"}' localhost:3000/api/spots/spot-3
```

Command-line build (no signing needed for the simulator):

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

That build only checks that the project compiles. An unsigned app has no Keychain access, so
login fails with a Keychain error if you install it; run from Xcode, or pass
`CODE_SIGN_IDENTITY=-` instead of `CODE_SIGNING_ALLOWED=NO`, to try the app.

App Transport Security allows plain HTTP to local hosts through `NSAllowsLocalNetworking` in
`UniEats/Info.plist`; since iOS 17 this is also what lets a device reach a server on the LAN
(e.g. `http://192.168.1.20:3000/api`).

The `UniEats/` folder is a synchronized group: new Swift files are compiled automatically.
Swift 6 with main-actor default isolation and approachable concurrency (the Xcode 26+ template
defaults).
