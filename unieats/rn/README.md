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

Screen readers: each list card's main area is one button whose label reads name, category, open/closed,
rating and price (the thumbnail is decorative); the favourite heart is a separate sibling button (not
nested inside the card's button, so both stay reachable) that reports its selected state; category chips
read as "<name> category" buttons with a selected state and the search field is labelled "Search spots";
on the detail screen the rating and review stars read as "out of 5"
and the header photo is labelled; the tab-bar emoji is hidden because the tab already announces its title.

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

Checks:

```bash
CI=1 npx expo customize tsconfig.json && git checkout -- tsconfig.json   # generates .expo/types (typed routes)
npx tsc --noEmit
npx expo install --check     # versions match Expo SDK 57
npx expo-doctor
```

Without `.expo/types` (written by `expo start` or the `customize` line above) `tsc` still passes but does not
check route hrefs. The committed `tsconfig.json` is already in the form both commands write, so the
`git checkout` is only a safety net.

`react-dom`, `react-native-reanimated` and `react-native-worklets` are not used by the app yet (no web target;
animations arrive in l10), but they stay in `package.json` pinned to the SDK 57 versions: expo-router's
dependencies declare them as peers, and without the pins npm resolves releases that need a newer React /
React Native and `npm ci` fails.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
