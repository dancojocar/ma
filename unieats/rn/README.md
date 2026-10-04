# UniEats – React Native (Expo SDK 57)

Tag `l06-async`: everything from l05 (server list with `useInfiniteQuery` keyed
`["spots", q, category]`, Zod, loading/error/empty states, pull-to-refresh, photos, read-only reviews,
search/filter/favourites) plus live updates: `src/hooks/useLiveSpots.ts` opens `ws://<host>:3000/live`
while the list is focused and the app is in the foreground, reconnects with exponential backoff
(1 s → 30 s), and on every `spot.created/updated/deleted` calls
`queryClient.invalidateQueries({ queryKey: ["spots"] })`. The header shows "● Live".
Demo: `curl -X PATCH localhost:3000/api/spots/spot-3 -H 'Content-Type: application/json' -d '{"rating":5}'`.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # scan the QR code with Expo Go (SDK 57), or press a (Android emulator) / i (iOS simulator)
```

The project `.npmrc` sets `legacy-peer-deps=false`, so `npm ci` installs the same complete tree on npm 10 and 11 even if your global `~/.npmrc` enables legacy peer deps.

Expo Go works for this tag (every module it uses ships inside Expo Go for SDK 57); the `unieats://` and https
deep links from l04 need a dev build: `npx expo run:android` (Android Studio's bundled Java 25 is fine: the
`plugins/withPrefabNativeAccess.js` config plugin makes the generated `android/gradlew` pass the JDK 24+
native-access flag that AGP 8.12's Prefab step needs).

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

## API base URL

Default: `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS
simulator (`src/api/config.ts`, chosen by `Platform.OS`). On a physical device set your laptop's LAN IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
