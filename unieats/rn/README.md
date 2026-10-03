# UniEats – React Native (Expo SDK 52)

Tag `l03-state`: the static 8-spot list now has search, a category filter and favourites. The state
lives in a plain Zustand store (`src/store/spotsStore.ts`: `searchQuery`, `categoryFilter`,
`favouriteIds`); `SearchBar` is stateless (`query` + `onQueryChange`) and the screen wires it to the
store. Try searching "pizza". No network yet.

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # press a (Android emulator) or i (iOS simulator)
```

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
