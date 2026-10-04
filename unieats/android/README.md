# UniEats Android — tag `l07-offline`

Offline-first: Room is the only thing the screens read. The network refreshes Room, the
WebSocket writes into Room, and your edits go to Room first and to an outbox second, which a
WorkManager job replays when the device is online.

## Run

```bash
cd unieats/server && npm install && npm start      # http://localhost:3000
./gradlew -p unieats/android :app:installDebug     # emulator reaches the laptop as 10.0.2.2
```

On a physical phone: `adb reverse tcp:3000 tcp:3000` and install with
`./gradlew -p unieats/android :app:installDebug -Punieats.serverHost=localhost:3000`.

## What to look at

- `data/local/` — `SpotEntity` (with `pendingSync`), `ReviewEntity`, `OutboxEntity`
  `{seq, opId, type, entityId, payload, attempted}`; `SpotDao.getPendingSync()` / `markSynced()`.
  `UniEatsDatabase` exports its schema (Room Gradle plugin, `room { schemaDirectory(...) }`) to
  `app/schemas/…/1.json`, which is committed. Any later schema change bumps `version` and adds an
  `autoMigrations` entry checked against these files.
- `data/repository/SpotRepository.kt` — `observe…()` flows from Room; `refreshSpots()` upserts by
  id and never overwrites a row with `pendingSync = true`; `editSpot()` updates the row
  optimistically and queues an `update` op, or merges into the spot's queued op if that one has
  not been sent yet. Live WebSocket events are written to Room; the socket is shared by the list
  and the detail screen and closes 5 s after neither is visible.
- `data/sync/` — `ConnectivityObserver` (`registerDefaultNetworkCallback` as a Flow; online means
  `INTERNET` **and** `VALIDATED`, the same test WorkManager's `CONNECTED` constraint uses), and
  `SyncScheduler`: after every edit it enqueues the one-shot `SyncWorker` with `APPEND_OR_REPLACE`
  (a run after the current one), and when the network comes back with `KEEP` (no duplicate if a
  run is already waiting or running). `OutboxSync.replay()` sends the ops in order with
  `Idempotency-Key = opId`; 409 → last-write-wins via `SyncConflictResolver` (the newer
  `updatedAt` wins, a tie goes to the client).
- Editing while a replay is in flight: before an op is sent it is marked `attempted` in the same
  transaction that reads it, so a new edit queues a second op instead of changing what was sent
  under that key. When the response arrives, `finish()` deletes the op by `opId` and only writes
  the server copy into the row if no other op is queued for the spot. If one is, the server took
  our earlier edit, so the newer op's base `updatedAt` moves to the server's new version and the
  next run sends it without a 409.
- UI: **Edit spot** on the detail screen (name, description, open now), a "Waiting to sync" label
  on edited spots and an "N changes pending" banner on the list.
- `AndroidManifest.xml` — `INTERNET`, `ACCESS_NETWORK_STATE` (connectivity callback), and
  `ACCESS_LOCAL_NETWORK`: from Android 17 (API 37) apps targeting it need this permission to reach
  the laptop at `10.0.2.2`. It is only declared here; the runtime request is added at `l09` next to
  the location prompt. Not verified on an API 37 device yet (the API 37 emulator crashes on the
  course machine); loopback is not "local network", so `adb reverse` + `localhost:3000` is the
  fallback. The manifest also removes WorkManager's default initializer, because
  `UniEatsApplication` provides a configuration with `HiltWorkerFactory`.

Unchanged from earlier labs: Navigation 3 with the two-pane layout and deep links,
`FavouritesRepository` shared by the list and the detail screen, pull-to-refresh on every state,
Material Symbols vector drawables via `painterResource`, the TalkBack descriptions on the rating,
reviews and live badge, and the light/dark theme.

## Demo: offline edit → reconnect → sync

1. Open a spot, tap **Edit spot**, change the name, Save — the list shows it immediately.
2. Turn on airplane mode (or stop the server), edit again: "1 change pending" stays visible.
3. Turn the network back on (or restart the server): the banner disappears, and the server log shows
   one `PATCH` with the `Idempotency-Key` header.
4. Conflict: edit a spot offline, change the same spot with curl
   (`curl -X PATCH localhost:3000/api/spots/spot-3 -H 'Content-Type: application/json' -d '{"name":"Server name"}'`),
   then go online — the server's newer version wins (409 → the row is replaced, the op dropped).

Kill and relaunch the app at any point: cached spots show instantly and the outbox survives.

An emulator whose network has no internet (only the laptop is reachable) never counts as online:
the network is not `VALIDATED`, so neither the connectivity trigger nor WorkManager runs the sync.
