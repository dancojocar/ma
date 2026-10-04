# UniEats – React Native (Expo SDK 57)

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
npx expo start        # scan the QR code with Expo Go (SDK 57), or press a (Android emulator) / i (iOS simulator)
```

The project `.npmrc` sets `legacy-peer-deps=false`, so `npm ci` installs the same complete tree on npm 10 and 11 even if your global `~/.npmrc` enables legacy peer deps.

Expo Go works for this tag (it uses only modules that ship inside Expo Go for SDK 57); only the `unieats://` and
https deep links above need the dev build:
`npx expo run:android` (Android Studio's bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config
plugin makes the generated `android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab step needs).

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
