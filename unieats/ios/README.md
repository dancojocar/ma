# UniEats iOS — l08-auth

Login + protected mutations on top of the offline-first app (l07).

## What this tag adds

- `Views/LoginView.swift` — email/password (`student@unieats.app` / `password`); the app shows
  it whenever there is no token (`UniEatsApp` switches on `SessionStore.isLoggedIn`).
- `ViewModels/SessionStore.swift` — `POST /api/auth/login`, keeps the JWT in memory and in the
  Keychain, `logout()` ("Log out" in the list toolbar), `sessionExpired()`.
- `Keychain/KeychainHelper.swift` — generic-password item with
  `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` (not in backups, not migrated to a new device);
  `SecItemUpdate` then `SecItemAdd`, status codes checked.
- `ApiClient` sends `Authorization: Bearer <jwt>` on every mutation (outbox PATCH replay and
  review POST). Any 401 → `ApiError.unauthorized` → `sessionExpired()` → back to the login
  screen with "Your session expired"; queued outbox entries are kept and replay after login.
- **Add review** (first appears here, POST needs auth): "Add review" in the detail's
  "Reviews (N)" section → stars + text → `POST /api/spots/:id/reviews` with an
  `Idempotency-Key`; the new review is stored in SwiftData and shows immediately.

Demo of expiry: restart the server with another secret (`JWT_SECRET=rotated npm start`), edit a
spot → the app returns to login; log in again → the pending edit syncs.

## From l07 (offline-first, unchanged)

Offline-first: SwiftData is the only thing the UI reads; the server feeds it, and edits made
offline are queued in an outbox and replayed when the server is reachable again.

- `Models/SpotEntity.swift`, `ReviewEntity.swift`, `OutboxEntry.swift` — SwiftData `@Model`s.
  `SpotEntity.pendingSync` flags rows with unsynced edits; `OutboxEntry` stores
  `{opId, type: "update", entityId, payload}` and survives restarts.
- `Repository/SpotRepository.swift` (`@MainActor`, uses the container's main context)
  - reads: `fetchPage` / `refreshSpot` / `refreshReviews` / live updates **upsert by id** and
    never overwrite a row whose `pendingSync` is true;
  - write: `editSpot(id:name:description:openNow:)` updates the row optimistically, sets
    `pendingSync`, and enqueues a PATCH whose payload carries the `updatedAt` the user edited
    (a second edit before sync merges into the same outbox entry);
  - `sync()` replays the outbox in order with `Idempotency-Key = opId`; 2xx → `markSynced`;
    409 → last-write-wins: the server's newer spot from the response replaces the row and the
    op is dropped; transport errors stop the replay and retry with backoff (5 s … 60 s).
- Replay triggers: app launch, `NWPathMonitor` reconnect (`Network/ConnectivityMonitor.swift`),
  the live WebSocket reconnecting (server back up), "Sync now", and every edit.
- UI: `@Query` in `SpotListView` / `SpotDetailView` (search + category become a SwiftData
  `#Predicate`); "Edit spot" on the detail screen (name, description, open now); orange
  "N changes pending" banner (`@Query` over the outbox) and a clock icon on pending rows.
- Live updates (l06), search, category filter, favourites, deep links and pagination kept.

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
open UniEats.xcodeproj    # pick an iPhone simulator, Cmd+R
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

Base URL `http://localhost:3000/api`, WebSocket `ws://localhost:3000/live`; override both with
the scheme environment variable `UNIEATS_API_URL`.
