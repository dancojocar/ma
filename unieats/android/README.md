# UniEats Android — tag `l03-state`

The list now has search, a category filter and favourites, all owned by a ViewModel.

## Run

```bash
./gradlew -p unieats/android :app:installDebug
```

## What to look at

- `ui/spotlist/SpotListViewModel.kt` — `SpotListUiState(spots, searchQuery, categoryFilter,
  favouriteIds)` held in a **private** `MutableStateFlow` and exposed as one read-only
  `StateFlow<SpotListUiState>`. The screen can only change it through `onSearchQueryChange`,
  `onCategorySelected` and `toggleFavourite`.
- `ui/spotlist/SpotSearchBar.kt` — stateless `SpotSearchBar(query, onQueryChange)` and
  `CategoryFilterRow(selected, onSelect)`: state goes down, events go up.
- `ui/spotlist/SpotListScreen.kt` — `SpotListScreen` collects the state with
  `collectAsStateWithLifecycle()` and hands it to the stateless `SpotListContent`. The Sort toggle
  stays a local `rememberSaveable`: it is ephemeral UI state nobody else needs.
- `data/repository/SpotRepository.kt` — still the 8 hard-coded spots; Hilt injects it.

**Rotation demo:** search for "pizza", then rotate the emulator. The query and the filtered list
survive because they live in the ViewModel (the manifest has no `configChanges`, so the Activity
really is recreated). Move the query into a `remember { mutableStateOf("") }` inside the composable
and rotate again to see it reset.
