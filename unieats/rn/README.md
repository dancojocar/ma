# UniEats – React Native (Expo SDK 57)

Tag `l14-tests`: the complete app plus its tests.

- **Unit tests** (Jest 29, `jest-expo` preset, which on SDK 57 needs the `@react-native/jest-preset` dev
  dependency): `src/__tests__/syncConflict.test.ts` (last-write-wins rule incl. the tie), `modelSchemas.test.ts`
  (Zod schemas from `@unieats/shared`), `sse.test.ts` (the streaming parser). Run `npm test`.
- **Maestro flow** `maestro/rn.yaml`: sign in → first spot (`testID="spot-list-item"`) → detail shows
  `Reviews (n)` (`spot-detail-reviews`) → "Describe this dish" (`describe-dish-button`) streams text → back.
  Needs the server running and a dev build on an emulator:

  ```bash
  npx expo run:android          # or: npx expo prebuild && (cd android && ./gradlew :app:assembleDebug)
  maestro test maestro/rn.yaml   # check only the syntax: maestro check-syntax maestro/rn.yaml
  ```

## What the app does (built up from l01 to l14)

| Tag | Feature | Where |
|---|---|---|
| l02 | `FlatList` + `keyExtractor`, `StyleSheet.create` | `app/(tabs)/spots/index.tsx`, `src/components/SpotCard.tsx` |
| l03 | Zustand store: search, category filter, favourites; stateless `SearchBar` | `src/store/spotsStore.ts` |
| l04 | expo-router `/spots/[id]`, deep links `unieats://spots/<id>`, `https://unieats.app/spots/<id>` | `app/(tabs)/spots/[id].tsx`, `app.json` |
| l05 | TanStack `useInfiniteQuery(["spots", q, category])`, Zod, retry 2, loading/error/empty, pull-to-refresh | `src/api/client.ts`, `src/hooks/useSpots.ts` |
| l06 | live updates over `ws://<host>:3000/live` with backoff | `src/hooks/useLiveSpots.ts` |
| l07 | expo-sqlite as the only UI source, "Edit spot" → outbox, NetInfo replay, 409 → server copy | `src/db/`, `src/repository/spotRepository.ts` |
| l08 | login, JWT in memory + expo-secure-store, Bearer on mutations, 401 → login, add review | `app/login.tsx`, `src/store/sessionStore.ts` |
| l09 | Nearby (2 km, location only while focused), local notification on `spot.updated` | `app/(tabs)/nearby.tsx`, `src/notifications/` |
| l10 | `FadeInDown` entrance, swipe-to-hide (gesture-handler + Reanimated) | `src/components/SwipeToDismiss.tsx`, `PERFORMANCE.md` |
| l11 | remote flag `show_new_rating_ui` + "Fetch & activate", `CrashReporter` + consent | `src/cloud/`, Profile tab |
| l12 | domain package `@unieats/shared` (models, schemas, `resolveConflict`, geo) | `shared/` |
| l13 | "Describe this dish" streamed over SSE (`expo/fetch`) | `src/api/aiClient.ts` |

Demo account: `student@unieats.app` / `password`.

## Run

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000). Then:

```bash
cd unieats/rn
npm ci
npx expo run:android      # dev build on the emulator (deep links, notifications, the Maestro flow)
# Expo Go (SDK 57) cannot load the app on Android: importing expo-notifications throws there
```

`npx expo run:android` works with Android Studio's bundled Java 25: the `plugins/withPrefabNativeAccess.js`
config plugin makes the generated `android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab
step needs. Expo Go is not an option on Android from l09 on: Expo Go for SDK 57 refuses to load expo-notifications
there (push support was removed in SDK 53), although UniEats only uses local notifications. The dev build also
carries the `app.json` plugin settings, the `unieats://` / https deep links and the Maestro `appId`.

Checks: `npx tsc --noEmit`, `npm test`, `npx expo-doctor`.

## API base URL

Default: `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS
simulator (`src/api/config.ts`, chosen by `Platform.OS`); the WebSocket is `ws://<same host>:3000/live`.
On a physical device set your laptop's LAN IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

## Demo helpers

- Emulator location (the seed is in Bucharest): `adb emu geo fix 26.1025 44.4268`; iOS simulator: Features →
  Location → Custom Location 44.4268, 26.1025.
- Token for curl: `TOKEN=$(curl -s localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"email":"student@unieats.app","password":"password"}' | jq -r .token)`
- Live update + notification: `curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"rating":4.9}'`
- Remote flag: `curl -X POST localhost:3000/api/config -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"flags":{"show_new_rating_ui":true}}'`, then Profile → Fetch & activate.
- Offline sync: edit a spot, turn on airplane mode, edit another, turn it off → both PATCHes reach the server.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the native and Flutter
UniEats apps.
