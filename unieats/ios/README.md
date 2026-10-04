# UniEats iOS — l10-polish

Animations, a profiling checklist, a String Catalog and accessibility fixes.

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
- **String Catalog** `UniEats/Localizable.xcstrings` (source language en). SwiftUI literals
  (`Text("…")`, `Button`, `Label`, `accessibilityLabel`) are keys already; strings built outside
  views use `String(localized:)`: `ApiError` messages, the `SessionStore` login/unlock notices,
  the Face ID prompt reasons and the "<spot> was updated" notification. Counts use plural
  variants instead of a hand-written ternary: `Text("\(outbox.count) changes pending")` in
  `Views/SyncStatusBanner.swift` has *one* = "%lld change pending", *other* = "%lld changes
  pending" (same for the review picker's "%lld stars"). Xcode adds new keys to the catalog when
  it builds the project in the IDE; a command-line build does not.
- **Accessibility**:
  - VoiceOver reads ratings and prices as words: the star + number in `SpotRow` and
    `SpotDetailView` is one element ("Rating 4.5 out of 5"), `$$` reads "Price level 2 of 3",
    review stars read "5 out of 5 stars" and each segment of the Add review picker "1 star" …
    "5 stars".
  - The selected category chip has the `.isSelected` trait.
  - Contrast of at least 4.5:1 (`Views/Palette.swift`): white text sits on a darker orange
    (`Color.brandFill`: chips, Log in, Unlock), and orange/green/red text on light tints uses
    darker shades in light mode (`brandText`, `openText`, `closedText`; dark mode keeps the
    system colours). This covers the badges, the Live indicator, the pending markers, Near Me
    distances and error text. The decorative logo images on Login and Unlock are hidden from
    VoiceOver.

Check it: `xcrun simctl ui booted appearance dark`, and Accessibility Inspector (Xcode › Open
Developer Tool) pointed at the simulator on the list, chips, detail and Add review sheet.

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
| l09 | Tabs (Spots, Near Me) with shared favourites; a deep link selects the Spots tab | `UniEatsApp.swift`, `ViewModels/FavouritesStore.swift` |
| l09 | Near Me tab (CoreLocation when-in-use, 2 km, updates only while visible), local notification from live `spot.updated` | `Views/NearMeView.swift`, `Location/LocationManager.swift`, `Notifications/SpotChangeNotifier.swift` |
| l09 | Background replay of pending edits (`BGAppRefreshTask` `com.unieats.app.outbox-sync`, scheduled when the app goes to the background with edits queued) | `Repository/SpotRepository.swift`, `UniEatsApp.swift`, `Info.plist` |

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
