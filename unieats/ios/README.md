# UniEats iOS — l11-cloud

A remote-config feature flag and a crash-reporting hook, without any Firebase dependency
(the deck's legacy demos show the real Remote Config / Crashlytics SDKs).

## What this tag adds

- **Settings tab** (`Views/SettingsView.swift`).
- **Remote config** (`Cloud/RemoteConfig.swift`): flag `show_new_rating_ui`, default `false`;
  **Fetch & activate** calls `GET /api/config` and applies the value at once. When `true`, spot
  rows show the new gradient rating badge (`NewRatingBadge` in `Views/SpotRow.swift`) instead of
  the plain star.
- **Crash reporting** (`Cloud/CrashReporter.swift`): `CrashReporter` protocol with a default
  `LoggingCrashReporter` (os `Logger`), wrapped by `CrashReportingConsent` — an opt-in
  "Share crash reports" toggle persisted in `UserDefaults`; nothing is recorded while it is off.
  Debug builds show **Test crash**, which calls `recordError(TestCrash(), …)`.
- **Log out** moved from the list toolbar into Settings › Account.

Demo:

```bash
# flip the flag on the server (token: see Run below)
curl -X POST -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"flags":{"show_new_rating_ui":true}}' localhost:3000/api/config
```

Settings → **Fetch & activate** → `true` → Spots shows the "★ 4.7 NEW" badges. Test crash with
the toggle off says "Not sent"; switch it on and the error appears in the Xcode console.

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
