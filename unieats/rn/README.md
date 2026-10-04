# UniEats – React Native (Expo SDK 57)

Tag `l02-ui`: a static list of the 8 canonical campus spots (hard-coded in `src/data/seeds.ts`)
with a detail view. Selection is local `useState` in `app/(tabs)/spots/index.tsx`; the list is a
`FlatList` with `keyExtractor` and every style goes through `StyleSheet.create`. No network yet.

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # scan the QR code with Expo Go (SDK 57), or press a (Android emulator) / i (iOS simulator)
```

Expo Go works for this tag: it uses only modules that ship inside Expo Go for SDK 57. A dev build works too:
`npx expo run:android` (Android Studio's bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config
plugin makes the generated `android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab step needs).

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
