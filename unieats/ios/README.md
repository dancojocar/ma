# UniEats iOS — l02-ui

Static list + detail from hard-coded data (no network yet).

- `UniEats/Models/Spot.swift` — `Spot` and `sampleSpots`, the 8 canonical campus spots
  (`spot-1` … `spot-8`, same data as the server seed and the other three stacks).
- `UniEats/Views/SpotListView.swift` — `List(sampleSpots) { … }` (the SwiftUI counterpart of
  `LazyColumn` / `ListView.builder` / `FlatList`; `Spot: Identifiable` is the key) and a
  `@State selectedSpot` that presents `SpotDetailView` as a sheet.
- `SpotRow` / `SpotDetailView` load the photo with `AsyncImage` from `photoUrl`.

## Run

`open UniEats.xcodeproj`, pick an iPhone simulator, Cmd+R. Or:

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

The `UniEats/` folder is a synchronized group: new Swift files are compiled automatically.
