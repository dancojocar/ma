# UniEats iOS — l12-kmp

A shared domain module consumed by the app.

## What this tag adds

- `UniEatsDomain/` — a local Swift package (`Package.swift`, swift-tools 6.0) **linked into the
  UniEats target** (Xcode › project › Package Dependencies shows it; `project.pbxproj` has an
  `XCLocalSwiftPackageReference` + product dependency). It holds the domain model `Spot` and
  `SpotConflictResolver` (last-write-wins: server wins only if its `updatedAt` is strictly
  newer; a tie goes to the client — CONTRACT §5).
- The app imports it everywhere it uses `Spot` (`import UniEatsDomain`); JSON/SwiftData mapping
  stays in the app (`Models/Spot.swift` `SpotDTO`, `Models/SpotEntity.swift`).
- `SpotRepository.sync()` asks `SpotConflictResolver.winner(local:server:)` on a 409: server →
  take the server row; client → rebase the queued PATCH on the server's `updatedAt` and resend.
- The original hand-written duplicate `UniEats/Domain/DomainBridge.swift` is gone.

Why a Swift package and not the Kotlin Multiplatform `UniEatsShared.xcframework`: the iOS build
must not depend on a JDK + Gradle build of `../android/shared`. To try the KMP route, build the
framework with `cd ../android && ./gradlew :shared:assembleUniEatsSharedXCFramework` and drag
`shared/build/XCFrameworks/debug/UniEatsShared.xcframework` into the target's
*Frameworks, Libraries, and Embedded Content*; the models then come from Kotlin instead.

Build the package on its own: `swift build --package-path UniEatsDomain`.

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
