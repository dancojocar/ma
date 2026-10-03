# UniEats – React Native (Expo SDK 52)

Tag `l07-offline`: offline-first. SQLite (`expo-sqlite`, `src/db/`) is the only thing screens read;
TanStack Query (`useSpotsSync`, keyed `["spots", q, category]`) now just fetches pages and upserts them
into SQLite without touching rows that have unsynced edits. "Edit spot" on the detail screen
(name / description / open now) updates the row optimistically (`pendingSync = 1`) and appends
`{ opId, type: "update", entityId, payload }` to the `outbox` table. `src/repository/spotRepository.ts`
`syncPending()` replays the outbox in order with `Idempotency-Key: <opId>` whenever
`@react-native-community/netinfo` reports the server reachable again; a 409 is resolved by
last-write-wins on `updatedAt` (`src/domain/syncConflict.ts`). The banner shows
"Offline · N changes pending". Live updates (`/live`) keep working and are written into SQLite.
Still from earlier tags: infinite scroll, pull-to-refresh, search/filter/favourites, read-only reviews.

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
