# UniEats iOS — l09-push-location

"Spots near me" and change notifications.

## What this tag adds

- **Near Me tab** (`Views/NearMeView.swift`, `Location/LocationManager.swift`): CoreLocation
  with a when-in-use permission request (`NSLocationWhenInUseUsageDescription` in
  `UniEats/Info.plist`); lists spots within **2 km**, nearest first, from SwiftData.
  `startUpdatingLocation()` runs only while the screen is visible (`onAppear`) and
  `stopUpdatingLocation()` is called in `onDisappear`.
- **Local notification "<spot> was updated"** (`Notifications/SpotChangeNotifier.swift`): fired
  from the l06 live `spot.updated` event, not from opening any screen. Permission is requested
  after login; the notification-center delegate shows the banner while the app is in the
  foreground (the live socket only runs then). No APNs/FCM: real push is a legacy demo.

Demo (simulator):

```bash
xcrun simctl location booted set 44.427,26.103   # campus centre, Bucharest
```

Open **Near Me** → allow location → spots sorted by distance. Back on **Spots**, PATCH a spot
with curl (below) → banner "Pizza Stop (new oven) was updated".

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter, favourites) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay, 409 last-write-wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login, add review | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift` |

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
