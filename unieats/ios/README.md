# UniEats iOS — l04-nav

Navigation + deep links on top of the l03 list (search, category filter and favourites kept).

- `SpotListView` owns a `NavigationStack(path:)` whose path is `[String]` — the stack pushes the
  spot **ID**, not the `Spot` object. `NavigationLink(value: spot.id)` +
  `.navigationDestination(for: String.self) { SpotDetailView(spotId: $0) }`.
- `SpotDetailView(spotId:)` resolves the spot itself (and shows "Spot not found" for a bad id).
- Deep links: `.onOpenURL` → `DeepLink.spotId(from:)` → `path = [id]`.
  - `unieats://spots/<id>` — registered in `UniEats/Info.plist` (`CFBundleURLTypes`).
  - `https://unieats.app/spots/<id>` — `applinks:unieats.app` in `UniEats/UniEats.entitlements`.
    A universal link only opens the app once `https://unieats.app/.well-known/apple-app-site-association`
    lists the app's Team ID + bundle id; without that file use the custom scheme for the demo.

## Run

`open UniEats.xcodeproj`, pick an iPhone simulator, Cmd+R. Then, with the app installed:

```bash
xcrun simctl openurl booted "unieats://spots/spot-3"    # opens Pizza Stop directly
```

Command-line build:

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```
