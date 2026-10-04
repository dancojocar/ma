# UniEats iOS — l11-cloud

A remote-config feature flag and a crash-reporting hook, without any Firebase dependency
(the deck's legacy demos show the real Remote Config / Crashlytics SDKs).

## What this tag adds

- **Settings tab** (`Views/SettingsView.swift`, `AppTab.settings` in `UniEatsApp.swift`).
- **Remote config** (`Cloud/RemoteConfig.swift`): flag `show_new_rating_ui`, default `false`;
  **Fetch & activate** calls `GET /api/config` and applies the value at once. The app passes
  `RemoteConfig` down through the SwiftUI environment; when the flag is `true`, spot rows show the
  new gradient rating badge (`NewRatingBadge` in `Views/SpotRow.swift`, white on the
  contrast-checked `brandFill` → `accentFill` from `Views/Palette.swift`, read by VoiceOver as
  "Rating 4.7 out of 5, new rating badge") instead of the plain star.
- **Crash reporting** (`Cloud/CrashReporter.swift`): `CrashReporter` protocol with a default
  `LoggingCrashReporter` (os `Logger`), wrapped by `CrashReportingConsent` — an opt-in
  "Share crash reports" toggle persisted in `UserDefaults`; nothing is recorded while it is off.
  Debug builds show **Test crash**, which calls `recordError(TestCrash(), …)`.
- **Settings › Account** now holds what the l08 Account menu on the list did: the
  "Unlock with Face ID" switch (shown only when biometrics are enrolled) and **Log out**
  (`SpotRepository.logOut()`, which also discards unsent edits). Logging out returns to the
  Spots tab for the next login.
- The new texts are in the String Catalog; the non-SwiftUI ones (`TestCrash`, the Test crash
  result) use `String(localized:)`.

Demo:

```bash
# flip the flag on the server (token: see Run below)
curl -X POST -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"flags":{"show_new_rating_ui":true}}' localhost:3000/api/config
```

Settings → **Fetch & activate** → `true` → Spots shows the "4.7 NEW" badges. Test crash with
the toggle off says "Not sent"; switch it on and the error appears in the Xcode console.

## Already in the app (earlier tags)

| Tag | Feature | Where |
|---|---|---|
| l03 | `@Observable` list state (search, category filter) | `ViewModels/SpotListViewModel.swift` |
| l04 | `NavigationStack` pushing spot ids; `unieats://spots/<id>` + `https://unieats.app/spots/<id>` | `Views/SpotListView.swift`, `Navigation/DeepLink.swift`, `Info.plist`, `UniEats.entitlements` |
| l05 | URLSession client, pagination, loading/error/empty states, pull-to-refresh | `Network/ApiClient.swift` |
| l06 | Live updates over `ws://localhost:3000/live`, reconnect with backoff | `Network/LiveUpdateService.swift` |
| l07 | SwiftData source of truth, "Edit spot" → outbox → replay (launch, reconnect, every edit, retry with backoff), edits during a sync sent in the same run, 409 last-write-wins, "N changes pending" | `Repository/SpotRepository.swift` |
| l08 | Login, JWT in Keychain, Bearer on mutations, 401 → login (queued edits kept, each records its user), Log out (Settings tab from l11) discards them, add review with one Idempotency-Key per draft, deep link kept while logged out or locked | `ViewModels/SessionStore.swift`, `Keychain/KeychainHelper.swift`, `Models/ReviewDraft.swift` |
| l08 | Opt-in biometric unlock (Settings › Account from l11): `.biometryCurrentSet` Keychain item read with an evaluated `LAContext`, "Unlock UniEats" screen, "Use password instead" logs out | `Keychain/BiometricAuth.swift`, `Views/UnlockView.swift` |
| l09 | Tabs (Spots, Near Me) with shared favourites; a deep link selects the Spots tab | `UniEatsApp.swift`, `ViewModels/FavouritesStore.swift` |
| l09 | Near Me tab (CoreLocation when-in-use, 2 km, updates only while visible), local notification from live `spot.updated` | `Views/NearMeView.swift`, `Location/LocationManager.swift`, `Notifications/SpotChangeNotifier.swift` |
| l09 | Background replay of pending edits (`BGAppRefreshTask` `com.unieats.app.outbox-sync`, scheduled when the app goes to the background with edits queued) | `Repository/SpotRepository.swift`, `UniEatsApp.swift`, `Info.plist` |
| l10 | Animated filtered list, row photo → detail zoom transition, `PERFORMANCE.md` | `Views/SpotListView.swift`, `Views/SpotRow.swift` |
| l10 | String Catalog with plural variants, `String(localized:)` outside views; VoiceOver labels for ratings and prices, `.isSelected` chips, 4.5:1 contrast colours | `Localizable.xcstrings`, `Views/Palette.swift` |

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
