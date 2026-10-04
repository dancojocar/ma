# UniEats iOS — l10-polish

Animations and a profiling checklist.

## What this tag adds

- Filtered list animates: `.animation(.easeInOut(duration: 0.25), value: spots.map(\.id))` on the
  `List` in `Views/SpotListView.swift`, rows use
  `.transition(.move(edge: .leading).combined(with: .opacity))` — tap a category chip or type in
  search and rows slide/fade instead of jumping.
- Shared-element style hero: the row photo is a `matchedTransitionSource(id: spot.id, in:)` and
  the pushed detail uses `.navigationTransition(.zoom(sourceID:in:))` (iOS 18), so the photo
  zooms into the detail screen and back. (`matchedGeometryEffect` only works between views that
  coexist in one hierarchy, e.g. a `ZStack`; across a `NavigationStack` push the zoom
  transition is the supported API.)
- `PERFORMANCE.md` — Instruments steps (SwiftUI, Animation Hitches, Time Profiler, Allocations).

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter, favourites) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay, 409 last-write-wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login, add review | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift` |
| l09 | Near Me tab (CoreLocation, 2 km), local notification from live `spot.updated` | `Views/NearMeView.swift`, `Notifications/SpotChangeNotifier.swift` |

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
