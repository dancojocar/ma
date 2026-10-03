# UniEats iOS — l14-tests

Unit tests (Swift Testing) and one Maestro UI flow.

## What this tag adds

- **`UniEatsTests` target + shared scheme** (also wired into the `UniEats` scheme, so ⌘U works):
  - `SpotConflictResolverTests` — server newer → server; local newer → client; tie → client.
  - `SpotDTOMappingTests` — CONTRACT JSON → `SpotDTO` → domain `Spot`; encoded field names;
    outbox PATCH payload shape.
  - `OfflineUpsertTests` — in-memory SwiftData: live updates upsert synced rows but never
    overwrite a `pendingSync` row; two offline edits coalesce into one outbox entry.
  - `DeepLinkTests` — both link forms accepted, other hosts/paths rejected.
- **`maestro/ios.yaml`** — login → first spot (`spot-list-item`) → "Reviews (N)"
  (`spot-detail-reviews`) → **Describe this dish** (`describe-dish-button`) → swipe back.
  Accessibility identifiers were added for exactly these selectors.
- An incompatible SwiftData store (e.g. left by another checkout of the app) is discarded and
  recreated instead of crashing at launch.

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEatsTests \
  -destination 'platform=iOS Simulator,name=iPhone 17' test
maestro check-syntax maestro/ios.yaml
cd ../server && npm start            # in another terminal, then with the app installed:
maestro test maestro/ios.yaml
```

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter, favourites) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay, 409 last-write-wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login, add review, log out (Settings tab from l11) | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift` |
| l09 | Near Me tab (CoreLocation, 2 km), local notification from live `spot.updated` | `Views/NearMeView.swift`, `Notifications/SpotChangeNotifier.swift` |
| l10 | Animated filtered list, row photo → detail zoom transition, `PERFORMANCE.md` | `Views/SpotListView.swift`, `Views/SpotRow.swift` |
| l11 | Settings: `show_new_rating_ui` Fetch & activate, consent-gated `CrashReporter`, debug Test crash | `Cloud/`, `Views/SettingsView.swift` |
| l12 | Local Swift package `UniEatsDomain` (domain `Spot`, `SpotConflictResolver`) linked into the app | `UniEatsDomain/` |
| l13 | "Describe this dish" streamed over SSE (`URLSession.bytes` + `AsyncThrowingStream`) | `AI/AiDescribeClient.swift` |

## Run

```bash
cd ../server && npm ci && npm start      # http://localhost:3000
open UniEats.xcodeproj                   # pick an iPhone simulator, Cmd+R
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

`UniEats/` is a synchronized folder: new Swift files are compiled without editing the project.
