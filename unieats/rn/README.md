# UniEats – React Native (Expo SDK 57)

Tag `l06-async`: everything from l05 (server list with `useInfiniteQuery` keyed
`["spots", q, category]`, Zod, loading/error/empty states, pull-to-refresh, photos, read-only reviews,
search/filter/favourites) plus live updates: `src/hooks/useLiveSpots.ts` opens `ws://<host>:3000/live`
while the list is focused and the app is in the foreground, reconnects with exponential backoff
(1 s → 30 s), and on every `spot.created/updated/deleted` calls
`queryClient.invalidateQueries({ queryKey: ["spots"] })`. Below the filters the list shows "● Live"
(or "○ Reconnecting…").
`src/api/queryClient.ts` feeds `AppState` into TanStack's `focusManager`, so stale queries (30 s) also
refetch when the app returns to the foreground.
Demo: `curl -X PATCH localhost:3000/api/spots/spot-3 -H 'Content-Type: application/json' -d '{"rating":5}'`.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

Screen readers: each list card's main area is one button whose label reads name, category, open/closed,
rating and price (the thumbnail is decorative); the favourite heart is a separate sibling button (not
nested inside the card's button, so both stay reachable) that reports its selected state; category chips
read as "<name> category" buttons with a selected state and the search field is labelled "Search spots";
on the detail screen the rating and review stars read as "out of 5"
and the header photo is labelled; loading spinners are labelled "Loading"; the live indicator reads "Live updates on" or
"Reconnecting to live updates" and is a polite live region, so TalkBack announces status changes; the tab-bar emoji is hidden because the tab already announces its title.

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

## API base URL

Resolution order (`src/api/config.ts`):

1. `EXPO_PUBLIC_API_URL`, if set.
2. In development, the host Metro is served from (`Constants.expoConfig.hostUri`), port 3000. A phone
   that loaded the bundle from your laptop's LAN IP reaches the API on the same IP, so physical
   devices work without extra config. Skipped for `localhost`/`127.0.0.1` and tunnels (`*.exp.direct`).
3. `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS simulator.

The live-updates socket (`LIVE_URL`) is derived from the same base: `http` → `ws`, `/api` → `/live`.

Override explicitly when needed:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

Release Android builds block cleartext HTTP (Expo allows it only in the debug manifest), so a release
build needs an `https://` `EXPO_PUBLIC_API_URL`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
