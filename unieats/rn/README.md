# UniEats – React Native (Expo SDK 57)

Tag `l09-push-location`: a **Nearby** tab (`app/(tabs)/nearby.tsx`) lists the spots stored in SQLite that
are within 2 km of you, nearest first (`src/domain/geo.ts`, haversine). Before asking it explains why it
needs location ("Allow location"), then asks with `expo-location` (`requestForegroundPermissionsAsync`); when
the OS will not ask again it shows "Open Settings" instead. The position is watched with `watchPositionAsync`
only while the tab is focused: `useFocusEffect` in `src/hooks/useNearbySpots.ts` removes the subscription on
blur and unmount. The tab also opens the live-updates socket while it is focused, like the Spots list.

Notifications are local, driven by the WebSocket (no FCM/APNs): when a live `spot.updated` event is news to
this device, i.e. not the echo of this device's own edit (`applyLiveEvent` in
`src/repository/spotRepository.ts` compares it with the SQLite row), `src/notifications/spotNotifications.ts`
shows "<spot> was updated" through `expo-notifications` (Android channel "Spot updates"). Once the session is
signed in, `app/(tabs)/_layout.tsx` asks for notification permission (POST_NOTIFICATIONS on Android 13+) and
listens for taps, which open the spot; the listener is removed when the session ends. `app.json` configures
both plugins (the iOS location usage text and the Android notification colour).

Emulator location (the seed is in Bucharest): `adb emu geo fix 26.1025 44.4268`; iOS simulator:
Features → Location → Custom Location 44.4268, 26.1025. Trigger a notification while the Spots or Nearby tab
is open:
`curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"rating":4.9}'`
(get `$TOKEN` from `POST /api/auth/login`).

Kept from l08: sign in (`student@unieats.app` / `password`) before the tabs open. The JWT from
`POST /api/auth/login` is kept in memory in a Zustand store (`src/store/sessionStore.ts`) and persisted with
`expo-secure-store` (`keychainAccessible: WHEN_UNLOCKED_THIS_DEVICE_ONLY`, `src/auth/sessionStorage.ts`).
Every mutation — outbox replay of "Edit spot" and the "Add a review" form
(`src/components/AddReviewForm.tsx`, `POST /spots/:id/reviews` with an `Idempotency-Key`) — sends
`Authorization: Bearer <jwt>`; a 401 (or an expired token at start-up) signs you out and returns to the login
screen with a notice. Reviews stay online-only: their `useMutation` in `app/(tabs)/spots/[id].tsx` sets
`networkMode: "always"`, so offline it fails at once with "Can't reach the server" instead of pausing in
`onlineManager`. The Profile tab (`app/(tabs)/profile.tsx`) shows the user and "Sign out".

Session and routing: `sessionStore` has four states, `restoring` (reading SecureStore at start-up),
`locked` (biometric unlock is on and has not happened yet), `signedOut` and `signedIn`. The root Stack in
`app/_layout.tsx` puts `(tabs)`, `unlock` and `login` behind `Stack.Protected` guards on that status, so no
screen redirects by hand: login, unlock, a 401 and "Sign out" only change the store, and when a guard turns
false the Stack falls back to its first screen, `app/index.tsx`, which redirects by status. The native splash
(`expo-splash-screen`) stays up while `restoring`; `(tabs)` is allowed during that time so a cold-start deep
link with a stored session opens the spot directly (`app/(tabs)/_layout.tsx` renders nothing until the
session is known, so the list and its WebSocket don't start under the splash).

Deep links while signed out or locked: `app/+native-intent.tsx` (`redirectSystemPath`) sees the launch URL
and every incoming link; when the user is not signed in it remembers a `/spots/<id>` link
(`src/auth/pendingLink.ts`, accepts `unieats://spots/<id>` and `https://unieats.app/spots/<id>`), and
`app/index.tsx` opens it instead of `/spots` once sign-in or unlock lands there.

Sign out vs. an ended session: every outbox op records the `user.id` of the account that queued it
(`outbox.userId`, migration v2 in `src/db/database.ts`). "Sign out" — and "Use password instead" on the
unlock screen — discards all queued edits and the optimistic rows they changed, then refetches
(`discardAllLocalChanges` in `src/db/localChanges.ts`); Profile and the unlock screen say how many unsynced
changes that will drop. A 401 or a biometric key invalidation keeps the queue, because usually the same
person signs back in and their edits replay; whenever a session starts (`startSession` in `sessionStore`),
ops queued by a different user are discarded first (`discardOtherUsersChanges`). A replay that is already
running stops as soon as the token changes, so nothing is sent with a token that is no longer current.

Biometric unlock (opt-in, off by default): Profile shows "Unlock with Face ID / Touch ID / fingerprint /
biometrics" only when `src/auth/biometrics.ts` finds strong biometrics enrolled
(`SecureStore.canUseBiometricAuthentication()` plus `expo-local-authentication`'s `hasHardwareAsync` and
`isEnrolledAsync`). Turning it on (`setBiometric` in `sessionStore`) moves the session into a second
SecureStore item written with `requireAuthentication: true` under its own `keychainService`
(`saveBiometricSession` in `src/auth/sessionStorage.ts`). On Android that item is encrypted with a Keystore
AES-GCM key that needs a `BIOMETRIC_STRONG` match for every use and is invalidated when a fingerprint or face
is enrolled, so the prompt appears while saving; on iOS it is a Keychain item with `.biometryCurrentSet`,
which iOS creates without asking, so `LocalAuthentication.authenticateAsync` confirms first. The plain item
is deleted and a non-secret flag tells the next start-up to show `app/unlock.tsx` ("Unlock UniEats"), which
reads the item: the OS prompt guards the read, and the token cannot be decrypted without a match (it is not
a yes/no check in JavaScript). Cancel or failure shows "Try again" and "Use password instead" (signs out).
After an enrollment change the item can no longer be read (`getItemAsync` resolves `null`), so the app
clears it and shows the login screen with "Biometrics on this device changed. Sign in with your password."
Turning the switch off writes the plain item back without a prompt. Sign out and a 401 delete both items
and the flag. `app.json` passes `faceIDPermission` to the `expo-secure-store` and
`expo-local-authentication` config plugins (`NSFaceIDUsageDescription` on iOS; `USE_BIOMETRIC` on Android).

Trying biometric unlock needs a dev build (`npx expo run:android` / `npx expo run:ios`); in Expo Go the switch
is hidden because Expo Go lacks this app's Face ID usage string. Android emulator: enroll a fingerprint in
Settings → Security, turn the switch on, and answer each prompt with `adb -e emu finger touch 1`. iOS
simulator: Features → Face ID → Enrolled, then Matching Face / Non-matching Face at the prompt. Simulators
do not enforce the prompt when the protected item is read (Expo docs), so check unlock on a real device.

Kept from l07: SQLite (`src/db/`) is the only thing screens read; "Edit spot" updates the row optimistically
and queues an outbox op that `syncPending()` (`src/repository/spotRepository.ts`) replays in order with
`Idempotency-Key: <opId>`, 409 resolved by last-write-wins, and a row is marked synced only if no newer edit
was queued while its server copy was being fetched. Hooks in `src/db/hooks.ts` read SQLite with
`useSyncExternalStore` over the per-table versions bumped by `notifyChanged` (`src/db/changes.ts`).
"Reachable" means `GET <API_URL>/health` answers 200 (`NetInfo.configure` with
`useNativeReachability: false`); `src/api/queryClient.ts` wires NetInfo into `onlineManager` (empty screens
say "You're offline…") and `AppState` into `focusManager`. Also kept: "N changes pending", live updates,
infinite scroll, pull-to-refresh, search / category filter / favourites.

Demo: edit a spot, toggle airplane mode on the emulator, edit another, turn it off → both PATCHes arrive.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

Screen readers: each list card's main area is one button whose label reads name, category, open/closed,
rating and price, plus "pending sync" for an unsynced edit (the thumbnail is decorative); the favourite heart is a separate sibling button (not
nested inside the card's button, so both stay reachable) that reports its selected state; category chips
read as "<name> category" buttons with a selected state and the search field is labelled "Search spots";
on the detail screen the rating and review stars read as "out of 5"
and the header photo is labelled; loading spinners are labelled "Loading"; the live indicator reads "Live updates on" or
"Reconnecting to live updates" and is a polite live region, so TalkBack announces status changes, as does
the offline / pending-changes banner; the edit form's fields and "Open now" switch are labelled; the tab-bar emoji is hidden because the tab already announces its title.
The login fields are labelled "Email" and "Password" and the session notice is a polite live region; the
review star picker is a "Rating" radio group whose stars read "1 star" … "5 stars" with a checked state; the
biometric switch is labelled with its visible text. On Nearby the rationale title is a header, "Allow location" and
"Open Settings" are buttons, and each nearby spot is a button read as "<name>, <n> metres away".

## Run

```bash
cd unieats/rn
npm ci
npx expo run:android  # dev build on the emulator (see below for Expo Go)
```

The project `.npmrc` sets `legacy-peer-deps=false`, so `npm ci` installs the same complete tree on npm 10 and 11 even if your global `~/.npmrc` enables legacy peer deps.

Expo Go for SDK 57 can load this tag: `expo-notifications` only logs a warning there, because Expo Go on
Android has no remote push (since SDK 53) and UniEats uses local notifications only. Expo Go ignores this app's
`app.json` plugin settings, though, so the location and Face ID usage texts and the notification colour are
missing and the biometric switch is hidden. Use a dev build for this tag: `npx expo run:android` (Android
Studio's bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config plugin makes the generated
`android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab step needs). It also enables the
`unieats://` and https deep links from l04.

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
