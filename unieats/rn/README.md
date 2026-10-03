# UniEats – React Native (Expo SDK 52)

Tag `l02-ui`: a static list of the 8 canonical campus spots (hard-coded in `src/data/seeds.ts`)
with a detail view. Selection is local `useState` in `app/(tabs)/spots/index.tsx`; the list is a
`FlatList` with `keyExtractor` and every style goes through `StyleSheet.create`. No network yet.

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # press a (Android emulator) or i (iOS simulator)
```

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
