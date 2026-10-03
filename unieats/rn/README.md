# UniEats – React Native (Expo SDK 52)

Tag `l10-polish`: list rows enter with Reanimated 3 `FadeInDown` (staggered by index) and can be
swiped away (left or right) to hide them for the session: `src/components/SwipeToDismiss.tsx` uses a
`react-native-gesture-handler` `Gesture.Pan()` driving a `useSharedValue` with `withTiming`, all on the UI
thread; "Show N hidden" brings them back. Profiling steps: `PERFORMANCE.md`.

Kept from l09: the Nearby tab (2 km, `expo-location` watch only while focused) and a local notification
"<spot> was updated" from live `spot.updated` events (`expo-notifications`).

Emulator location (the seed is in Bucharest): `adb emu geo fix 26.1025 44.4268`; iOS simulator:
Features → Location → Custom Location 44.4268, 26.1025. Trigger a notification:
`curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"rating":4.9}'`
(get `$TOKEN` from `POST /api/auth/login`).

Kept from l08: login (`student@unieats.app` / `password`), JWT in memory + `expo-secure-store`, Bearer on
every mutation and outbox replay, 401 → login, add-review, profile / sign out.
Everything from l07 is kept: SQLite as the only UI source, outbox + NetInfo replay with
`Idempotency-Key`, 409 last-write-wins, "N changes pending", live updates, infinite scroll, pull-to-refresh,
search / category filter / favourites.

Demo: edit a spot, toggle airplane mode on the emulator, edit another, turn it off → both PATCHes arrive.

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
