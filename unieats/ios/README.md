# UniEats iOS — l08-auth

Login + protected mutations on top of the offline-first app (l07).

## What this tag adds

- `Views/LoginView.swift` — email/password (`student@unieats.app` / `password`).
- `ViewModels/SessionStore.swift` — `POST /api/auth/login`; keeps the JWT in memory and in the
  Keychain and `user.id` from the response in `UserDefaults`. `phase` is `.signedOut`, `.locked`
  or `.signedIn`, and `UniEatsApp` switches on it: `LoginView`, `UnlockView` or the spot list.
  `sessionExpired()` (any 401) ends the session with "Your session expired".
- `Keychain/KeychainHelper.swift` — one generic-password item. `save` deletes and re-adds it
  (`SecItemUpdate` cannot change access control): normally with
  `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` (not in backups, not migrated to a new device),
  with biometric unlock on with a `SecAccessControl` of
  `kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly` + `.biometryCurrentSet`. `load` never
  prompts; `hasItem()` checks for a session without reading it.
- `ApiClient` sends `Authorization: Bearer <jwt>` on every mutation (outbox PATCH replay and
  review POST). A 401 → `ApiError.unauthorized` → back to the login screen.
- **Log out** (Account menu, top left of the list) calls `SpotRepository.logOut()`: it deletes
  the Keychain item and also discards the outbox and the rows that were only showing unsynced
  edits (they are reloaded from the server), so the next person to log in on the device never
  sends them. A 401 keeps queued edits: each `OutboxEntry` records the `userId` that made it,
  the same user's edits replay after logging in again, and `sync()` discards edits made by any
  other user.
- **Add review** (first appears here, POST needs auth): "Add review" in the detail's
  "Reviews (N)" section → stars + text → `POST /api/spots/:id/reviews` with an
  `Idempotency-Key`; the new review is stored in SwiftData and shows immediately.
  `Models/ReviewDraft.swift` keeps one key per draft: posting again after a timeout or 5xx reuses
  it (the server answers with the review it already created), changing stars or text gets a new
  one (the server caches every response below 500, so an old 4xx would otherwise come back).
- Deep links: `.onOpenURL` sits on the root of `UniEatsApp`, so a link opened while logged out
  or locked is kept (`pendingSpotId`) and opened after login or unlock.

### Biometric unlock (opt-in, off by default)

- After logging in, Account › **Unlock with Face ID** (Touch ID / Optic ID on those devices). The
  toggle only appears when `canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics)` is true,
  i.e. biometrics are enrolled. Turning it on asks for Face ID, then re-stores the token as the
  `.biometryCurrentSet` item; turning it off re-stores it as a normal item.
- `Keychain/BiometricAuth.swift` — `LAContext.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics,
  localizedReason: "Unlock UniEats")` returns the evaluated context; `KeychainHelper.load(context:)`
  passes it as `kSecUseAuthenticationContext`, so reading the token needs no second prompt. The
  token is bound to the biometric check by the Keychain, not by an app flag.
- Cold start with the flag on: `SessionStore` does not read the token, it goes to `.locked` and
  `Views/UnlockView.swift` ("Unlock UniEats") prompts at once. Success → straight into the app.
  Cancel or a failed match stays on that screen; **Use password instead** is an explicit log out
  (outbox discarded, as above) and shows the login screen.
- Enrolling a new face or finger invalidates the item. The app also compares
  `LAContext.domainState.biometry.stateHash` with the one stored when the toggle was turned on;
  either way it signs out (queued edits kept) and the login screen says "Biometrics changed on
  this device". Log out and a 401 delete the item and turn the toggle off.
- `NSFaceIDUsageDescription` is in `UniEats/Info.plist`.

Demo of expiry: restart the server with another secret (`JWT_SECRET=rotated npm start`), edit a
spot → the app returns to login; log in again → the pending edit syncs.

Face ID in the simulator: Features › Face ID › Enrolled, then Matching Face / Non-matching Face.
The same from a terminal:

```bash
xcrun simctl spawn booted notifyutil -s com.apple.BiometricKit.enrollmentChanged 1
xcrun simctl spawn booted notifyutil -p com.apple.BiometricKit.enrollmentChanged   # enrolled
xcrun simctl spawn booted notifyutil -p com.apple.BiometricKit_Sim.pearl.match     # matching face
xcrun simctl spawn booted notifyutil -p com.apple.BiometricKit_Sim.pearl.nomatch   # non-matching
```

The simulator stores the item with its access control but does not enforce it on reads, and
re-enrolling there does not change the state hash: check the protection and the
enrollment-change path on a device.

## From l07 (offline-first, unchanged)

Offline-first: SwiftData is the only thing the UI reads; the server feeds it, and edits made
offline are queued in an outbox and replayed when the server is reachable again.

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
