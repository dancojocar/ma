# UniEats – React Native (Expo SDK 52)

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
npx expo start        # press a (Android emulator) or i (iOS simulator)
```

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

## API base URL

Default: `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS
simulator (`src/api/config.ts`, chosen by `Platform.OS`). On a physical device set your laptop's LAN IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
