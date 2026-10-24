# UniEats Android — tag `l04-nav`

Real navigation with **Navigation 3**: the back stack is a list you own (`rememberNavBackStack`),
`NavDisplay` renders it, the detail screen receives only the spot **id**, two deep links open a spot
directly, and on a wide window (tablet, foldable, landscape) the list and the detail show side by side
through the Material `ListDetailSceneStrategy`. Search, category filter and favourites from l03 are
still there.

## Run

```bash
./gradlew -p unieats/android :app:installDebug
```

## What to look at

- `ui/navigation/NavGraph.kt` — `@Serializable` keys `SpotList` and `SpotDetail(spotId)` implement
  `NavKey`; `rememberNavBackStack(...)` is the back stack; `backStack.add(SpotDetail(id))` pushes and
  `backStack.removeLastOrNull()` pops; `NavDisplay(entryProvider = entryProvider { entry<SpotList> {…}
  entry<SpotDetail> { key -> … } })` maps keys to screens; `rememberListDetailSceneStrategy()` plus the
  `listPane()` / `detailPane()` metadata give the two-pane layout on wide windows.
- `ui/spotdetail/SpotDetailViewModel.kt` — a Hilt assisted ViewModel: the entry hands it the
  `spotId` from the key (`hiltViewModel<SpotDetailViewModel, Factory> { it.create(key.spotId) }`),
  and `rememberViewModelStoreNavEntryDecorator()` scopes one ViewModel per back-stack entry. It looks
  the spot up in the repository (it never gets the object itself, so a deep link and a tap take the
  same path).
- `ui/navigation/DeepLinks.kt` — two `UriDeepLinkMatcher`s (`unieats://spots/{spotId}`,
  `https://unieats.app/spots/{spotId}`) with `withBackStack { listOf(SpotList, it.key) }`;
  `backStackFor(intent)` turns the launch Intent into the initial keys, so Back after a deep link
  lands on the list. `MainActivity` is `singleTop` and handles `onNewIntent` for links that arrive
  while the app is running.
- `AndroidManifest.xml` — two `VIEW` + `BROWSABLE` intent-filters: `unieats://spots/…` and
  `https://unieats.app/spots/…`.

## Try the deep links (emulator or phone with USB debugging)

```bash
adb shell am start -W -a android.intent.action.VIEW -d "unieats://spots/spot-3" com.unieats.app
adb shell am start -W -a android.intent.action.VIEW -d "https://unieats.app/spots/spot-3" com.unieats.app
```

Both open "Pizza Stop"; Back goes to the list (`withBackStack` put `SpotList` underneath). An unknown
id shows "Spot not found". The https link is marked `autoVerify`, but without a
`/.well-known/assetlinks.json` on unieats.app Android will not verify it, so a browser tap opens the
browser; passing the package name as above (or
`adb shell pm set-app-links-user-selection --user 0 --package com.unieats.app true unieats.app`)
routes it to the app.

## Try the two-pane layout

Rotate the emulator to landscape, or run on a tablet / the resizable emulator: the list stays on the
left and the selected spot opens on the right (`ListDetailSceneStrategy`). Back closes the detail pane.
