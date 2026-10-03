# UniEats – React Native (Expo SDK 52)

Tag `l04-nav`: list → detail with expo-router. `app/(tabs)/spots/[id].tsx` is the route
`/spots/:id`; the list pushes `{ pathname: "/spots/[id]", params: { id } }` (the id, not the object) and
the detail looks the spot up. Search, category filter and favourites from l03 are kept.

Deep links (need a dev build, not Expo Go: `npx expo run:android`):

```bash
adb shell am start -W -a android.intent.action.VIEW -d "unieats://spots/spot-3" com.unieats.rn
adb shell am start -W -a android.intent.action.VIEW -d "https://unieats.app/spots/spot-3" com.unieats.rn
xcrun simctl openurl booted "unieats://spots/spot-3"
```

The https form is declared in `app.json` (`android.intentFilters`, `ios.associatedDomains`); without a
hosted `assetlinks.json` Android does not verify it, so pass the package name as above.

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # press a (Android emulator) or i (iOS simulator)
```

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
