# UniEats – React Native (Expo SDK 52)

Tag `l08-auth`: sign in (`student@unieats.app` / `password`) before the tabs open. The JWT from
`POST /api/auth/login` is kept in memory in a Zustand store (`src/store/sessionStore.ts`) and persisted with
`expo-secure-store` (`keychainAccessible: WHEN_UNLOCKED_THIS_DEVICE_ONLY`, `src/auth/sessionStorage.ts`).
Every mutation — outbox replay of "Edit spot" and the new "Add a review" form — sends
`Authorization: Bearer <jwt>`; a 401 (or an expired token at start-up) signs you out and returns to the login
screen with a notice. The Profile tab shows the user and "Sign out".

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
