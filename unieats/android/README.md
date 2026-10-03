# UniEats Android — tag `l04-nav`

Real navigation: a Navigation Compose `NavHost` with type-safe routes, the detail screen receives
only the spot **id**, and two deep links open a spot directly. Search, category filter and
favourites from l03 are still there.

## Run

```bash
./gradlew -p unieats/android :app:installDebug
```

## What to look at

- `ui/navigation/NavGraph.kt` — `@Serializable` routes `SpotList` and `SpotDetail(spotId)`;
  `navController.navigate(SpotDetail(id))`; the detail destination declares two `navDeepLink`s.
- `ui/spotdetail/SpotDetailViewModel.kt` — reads its argument with
  `savedStateHandle.toRoute<SpotDetail>()` and looks the spot up in the repository (it never gets
  the object itself, so a deep link and a tap take the same path).
- `AndroidManifest.xml` — two `VIEW` + `BROWSABLE` intent-filters: `unieats://spots/…` and
  `https://unieats.app/spots/…`.

## Try the deep links (emulator or phone with USB debugging)

```bash
adb shell am start -W -a android.intent.action.VIEW -d "unieats://spots/spot-3" com.unieats.app
adb shell am start -W -a android.intent.action.VIEW -d "https://unieats.app/spots/spot-3" com.unieats.app
```

Both open "Pizza Stop"; Back goes to the list (Navigation builds the back stack for you). An unknown
id shows "Spot not found". The https link is marked `autoVerify`, but without a
`/.well-known/assetlinks.json` on unieats.app Android will not verify it, so a browser tap opens the
browser; passing the package name as above (or
`adb shell pm set-app-links-user-selection --user 0 --package com.unieats.app true unieats.app`)
routes it to the app.
