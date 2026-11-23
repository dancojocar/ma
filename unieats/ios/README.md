# UniEats iOS — l03-state

Observable state: the list screen's state lives in one `@Observable` ViewModel.

- `UniEats/ViewModels/SpotListViewModel.swift` — `@Observable` class (main-actor through default
  isolation) owning `spots`, `searchQuery`, `categoryFilter` and `favouriteIds`, all
  `private(set)` (read-only from the outside, like a Kotlin `val`); views change them only
  through `onQueryChange`, `onCategoryChange` and `toggleFavourite`. `filteredSpots` filters case-insensitively.
- `SpotListView` holds the ViewModel in `@State` and wires `.searchable` to
  `searchQuery` / `onQueryChange`; `CategoryFilterView` is stateless
  (`selectedCategory` + `onCategoryChange`); `SpotRow` shows the favourite heart.

Demo: search "pizza" → only Pizza Stop remains; pick a category; tap a heart.

## Run

`open UniEats.xcodeproj` (Xcode 26 or later), pick an iPhone simulator, Cmd+R. Or:

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

The `UniEats/` folder is a synchronized group: new Swift files are compiled automatically.
Swift 6 with main-actor default isolation and approachable concurrency (the Xcode 26+ template
defaults).
