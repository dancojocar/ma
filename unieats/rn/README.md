# UniEats – React Native (Expo SDK 57)

Tag `l05-rest`: list and detail come from the UniEats server. `src/api/client.ts` (fetch + 10 s
timeout + Zod parsing into a typed `ApiError`), TanStack Query `useInfiniteQuery` keyed
`["spots", q, category]` (`src/hooks/useSpots.ts`) with `retry: 2`, infinite scroll that stops on
`hasNextPage: false`, pull-to-refresh, loading / error (Retry) / empty states and `photoUrl` images.
Reviews are read-only (`GET /api/spots/:id/reviews`); writing reviews needs login (l08).
Search, category filter and favourites (Zustand) are kept; search is debounced 300 ms.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # scan the QR code with Expo Go (SDK 57), or press a (Android emulator) / i (iOS simulator)
```

Expo Go works for this tag (it uses only modules that ship inside Expo Go for SDK 57); the `unieats://` and
https deep links from l04 need a dev build: `npx expo run:android` (Android Studio's bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config
plugin makes the generated `android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab step needs).

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

## API base URL

Default: `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS
simulator (`src/api/config.ts`, chosen by `Platform.OS`). On a physical device set your laptop's LAN IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
