# UniEats – React Native (Expo SDK 57)

Tag `l05-rest`: list and detail come from the UniEats server. `src/api/client.ts` (fetch + 10 s
timeout + Zod parsing into a typed `ApiError`), TanStack Query `useInfiniteQuery` keyed
`["spots", q, category]` (`src/hooks/useSpots.ts`) with `retry: 2`, infinite scroll that stops on
`hasNextPage: false`, pull-to-refresh, loading / error (Retry) / empty states and `photoUrl` images.
`src/api/queryClient.ts` feeds `AppState` into TanStack's `focusManager`, so stale queries (30 s) refetch
when the app returns to the foreground.
Reviews are read-only (`GET /api/spots/:id/reviews`); writing reviews needs login (l08).
Search, category filter and favourites (Zustand) are kept; search is debounced 300 ms.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

Screen readers: each list card's main area is one button whose label reads name, category, open/closed,
rating and price (the thumbnail is decorative); the favourite heart is a separate sibling button (not
nested inside the card's button, so both stay reachable) that reports its selected state; category chips
read as "<name> category" buttons with a selected state and the search field is labelled "Search spots";
on the detail screen the rating and review stars read as "out of 5"
and the header photo is labelled; loading spinners are labelled "Loading"; the tab-bar emoji is hidden because the tab already announces its title.

## Run

```bash
cd unieats/rn
npm ci
npx expo start        # scan the QR code with Expo Go (SDK 57), or press a (Android emulator) / i (iOS simulator)
```

The project `.npmrc` sets `legacy-peer-deps=false`, so `npm ci` installs the same complete tree on npm 10 and 11 even if your global `~/.npmrc` enables legacy peer deps.

Expo Go works for this tag (it uses only modules that ship inside Expo Go for SDK 57); the `unieats://` and
https deep links from l04 need a dev build: `npx expo run:android` (Android Studio's bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config
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

## API base URL

Resolution order (`src/api/config.ts`):

1. `EXPO_PUBLIC_API_URL`, if set.
2. In development, the host Metro is served from (`Constants.expoConfig.hostUri`), port 3000. A phone
   that loaded the bundle from your laptop's LAN IP reaches the API on the same IP, so physical
   devices work without extra config. Skipped for `localhost`/`127.0.0.1` and tunnels (`*.exp.direct`).
3. `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS simulator.

Override explicitly when needed:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

Release Android builds block cleartext HTTP (Expo allows it only in the debug manifest), so a release
build needs an `https://` `EXPO_PUBLIC_API_URL`.

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
