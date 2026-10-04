# UniEats iOS — l09-push-location

"Spots near me", change notifications and background replay of the outbox.

## What this tag adds

- **Tabs**: `UniEatsApp` shows a `TabView` (Spots, Near Me) once signed in. Favourites move out
  of `SpotListViewModel` into `ViewModels/FavouritesStore.swift`, owned by the app, so both tabs
  share them. A deep link selects the Spots tab before opening the spot, so a link opened while
  Near Me is showing does not land on a hidden stack.
- **Near Me tab** (`Views/NearMeView.swift`, `Location/LocationManager.swift`): CoreLocation
  with a when-in-use permission request (`NSLocationWhenInUseUsageDescription` in
  `UniEats/Info.plist`); lists spots within **2 km**, nearest first, from SwiftData
  (`SpotRepository.fetchAllPages()` fills the store first).
  `startUpdatingLocation()` runs only while the screen is visible (`onAppear`) and
  `stopUpdatingLocation()` is called in `onDisappear`.
- **Local notification "<spot> was updated"** (`Notifications/SpotChangeNotifier.swift`): fired
  from the l06 live `spot.updated` event, not from opening any screen. Permission is requested
  after login; the notification-center delegate shows the banner while the app is in the
  foreground (the live socket only runs then, while the Spots list is on screen). No APNs/FCM:
  real push is a legacy demo.
- **Background replay** of pending edits (`BGAppRefreshTask`, id `com.unieats.app.outbox-sync`):
  when the app goes to the background with edits still queued, `SpotRepository.scheduleBackgroundSync()`
  submits a `BGAppRefreshTaskRequest` (earliest in 15 minutes); the system decides when it runs.
  The `.backgroundTask(.appRefresh(…))` scene modifier in `UniEatsApp` registers the handler,
  which replays the outbox and schedules again if anything is left. `UniEats/Info.plist` lists
  the id under `BGTaskSchedulerPermittedIdentifiers` and has `UIBackgroundModes` = `fetch`.
  The replay needs the token in memory, so with biometric unlock on, a background launch from a
  cold start stays locked and leaves the edits for the next unlock.

Demo (simulator):

```bash
xcrun simctl location booted set 44.427,26.103   # campus centre, Bucharest
```

Open **Near Me** → allow location → spots sorted by distance. Back on **Spots**, PATCH a spot
with curl (below) → banner "Pizza Stop (new oven) was updated".

The simulator cannot run background tasks: `submit` fails with "not available on this platform"
(logged under `com.unieats.app` / `sync` and ignored). On a device, pause in the debugger after
sending the app to the background and run

```
e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.unieats.app.outbox-sync"]
```

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay (launch, reconnect, every edit, retry with backoff), edits during a sync sent in the same run, 409 last-write-wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login (queued edits kept, each records its user), Log out discards them, add review with one Idempotency-Key per draft, deep link kept while logged out or locked | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift`, `Models/ReviewDraft.swift` |
| l08 | Opt-in biometric unlock (Account menu): `.biometryCurrentSet` Keychain item read with an evaluated `LAContext`, "Unlock UniEats" screen, "Use password instead" logs out | `Keychain/BiometricAuth.swift`, `Views/UnlockView.swift` |

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
