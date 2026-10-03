# UniEats Android — tag `l08-auth`

Offline-first: Room is the only thing the screens read. The network refreshes Room, the
WebSocket writes into Room, and your edits go to Room first and to an outbox second, which a
WorkManager job replays when the device is online.

## Run

```bash
cd unieats/server && npm install && npm start      # http://localhost:3000
./gradlew -p unieats/android :app:installDebug     # emulator reaches the laptop as 10.0.2.2
```

(Phone: `adb reverse tcp:3000 tcp:3000` and add `-Punieats.serverHost=localhost:3000`.)

## What to look at

- `data/local/` — `SpotEntity` (with `pendingSync`), `ReviewEntity`, `OutboxEntity`
  `{seq, opId, type, entityId, payload}`; `SpotDao.getPendingSync()` / `markSynced()`.
- `data/repository/SpotRepository.kt` — `observe…()` flows from Room; `refreshSpots()` upserts by
  id and never overwrites a row with `pendingSync = true`; `editSpot()` updates the row
  optimistically and queues one `update` op per spot; live WebSocket events are written to Room
  (shared by the list and the detail screen, closed when neither is visible).
- `data/sync/` — `ConnectivityObserver` (`ConnectivityManager.registerDefaultNetworkCallback` as a
  Flow), `SyncScheduler` (enqueues the one-shot `SyncWorker` when the network comes back and after
  every edit), `OutboxSync.replay()` (in order, `Idempotency-Key = opId`, 409 → last-write-wins via
  `SyncConflictResolver`: the newer `updatedAt` wins, a tie goes to the client).
- UI: **Edit spot** on the detail screen (name, description, open now), a "Waiting to sync" chip on
  edited spots and an "N changes pending" banner on the list.

## Login and protected writes (new in l08)

- Sign in with `student@unieats.app` / `password`. The server protects every mutation from this
  tag on.
- `data/auth/TokenStore.kt` — the JWT and display name in `EncryptedSharedPreferences`
  (AES-256-GCM values, key in the Android Keystore). `allowBackup="false"` keeps it out of backups.
- `data/remote/ApiClient.kt` — `authorized()` adds `Authorization: Bearer <jwt>` to PATCH and
  review POST (reads stay anonymous); `OutboxSync` replays with the same header.
- `di/AppModule.kt` — a 401 on a request that carried a token clears the session, and
  `ui/navigation/NavGraph.kt` sends you back to the login screen (also when the 1 h token
  expires). Queued edits stay in the outbox and are replayed after the next sign-in.
- **Log out** is in the ⋮ menu of the list; **Add review** is on the detail screen (online only:
  POST needs the server).

## Demo: offline edit → reconnect → sync

1. Open a spot, tap **Edit spot**, change the name, Save — the list shows it immediately.
2. Turn on airplane mode (or stop the server), edit again: "1 change pending" stays visible.
3. Turn the network back on (or restart the server): the banner disappears, and the server log shows
   one `PATCH` with the `Idempotency-Key` header.
4. Conflict: edit a spot offline, change the same spot with curl
   (`curl -X PATCH localhost:3000/api/spots/spot-3 -H 'Content-Type: application/json' -d '{"name":"Server name"}'`),
   then go online — the server's newer version wins (409 → the row is replaced, the op dropped).

Kill and relaunch the app at any point: cached spots show instantly and the outbox survives.
