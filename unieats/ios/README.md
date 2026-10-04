# UniEats iOS — l07-offline

Offline-first: SwiftData is the only thing the UI reads; the server feeds it, and edits made
offline are queued in an outbox and replayed when the server is reachable again.

## What this tag adds

- `Models/SpotEntity.swift`, `ReviewEntity.swift`, `OutboxEntry.swift` — SwiftData `@Model`s.
  `SpotEntity.pendingSync` flags rows with unsynced edits; `OutboxEntry` stores
  `{opId, type: "update", entityId, payload}` and survives restarts.
- `Repository/SpotRepository.swift` (main-actor isolated by default, uses the container's main
  context)
  - reads: `fetchPage` / `refreshSpot` / `refreshReviews` / live updates **upsert by id** and
    never overwrite a row whose `pendingSync` is true;
  - write: `editSpot(id:name:description:openNow:)` updates the row optimistically, sets
    `pendingSync`, and enqueues a PATCH whose payload carries the `updatedAt` the user edited.
    A second edit merges into the queued entry unless that entry is being sent right now; then
    it is queued separately and rebased on the server's `updatedAt` once the first succeeds;
  - `sync()` replays the outbox in order with `Idempotency-Key = opId`; 2xx → `markSynced`.
    Edits made during a sync are sent in the same sync run;
  - 409 → last-write-wins on `updatedAt`: the server only answers 409 when its copy is newer
    than the version the user edited, so the server's spot from the response replaces the row
    and the op is dropped, together with any later queued edit of that spot (it was based on
    the same stale version);
  - another 4xx rejection drops the change and reloads the spot from the server (404 deletes
    the row); transport errors stop the replay and retry with backoff (5 s … 60 s).
- Replay triggers: app launch, `NWPathMonitor` reconnect (`Network/ConnectivityMonitor.swift`),
  the live WebSocket reconnecting (server back up), "Sync now", and every edit. Replay runs
  only while the app is in the foreground; background replay arrives in l09.
- UI: `@Query` in `SpotListView` / `SpotDetailView` (search on name or description + category
  become a SwiftData `#Predicate`); "Edit spot" on the detail screen (name, description, open
  now); orange "N changes pending" banner (`@Query` over the outbox) and a clock icon on
  pending rows.
- Live updates (l06, a failed socket is cancelled before reconnecting), search, category filter,
  favourites, deep links and pagination kept.

## Demo (simulator)

1. `cd ../server && npm start`, run the app, open Pizza Stop → **Edit spot** → Save: syncs at once.
2. Stop the server (Ctrl+C). Edit again → "1 change pending", the row shows the clock icon.
3. Start the server → within seconds the Live badge returns and the outbox replays
   (`curl localhost:3000/api/spots/spot-3` shows the edit).
4. Conflict: stop the server, edit Espresso Lab, start the server and immediately
   `curl -X PATCH -H 'Content-Type: application/json' -d '{"name":"Espresso SERVER"}' localhost:3000/api/spots/spot-2`
   → the replay gets 409 and the app shows the server's name (server `updatedAt` is newer).

On a device, airplane mode exercises the `NWPathMonitor` path instead.

## Run / build

```bash
open UniEats.xcodeproj    # Xcode 26 or later; pick an iPhone simulator, Cmd+R
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

Base URL `http://localhost:3000/api`, WebSocket `ws://localhost:3000/live`; override both with
the scheme environment variable `UNIEATS_API_URL`. App Transport Security allows plain HTTP to
local hosts through `NSAllowsLocalNetworking` in `UniEats/Info.plist`; since iOS 17 this is also
what lets a device reach a server on the LAN (e.g. `http://192.168.1.20:3000/api`).

The `UniEats/` folder is a synchronized group: new Swift files are compiled automatically.
Swift 6 with main-actor default isolation and approachable concurrency (the Xcode 26+ template
defaults).
