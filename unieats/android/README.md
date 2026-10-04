# UniEats Android — tag `l09-push-location`

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
  `{seq, opId, type, entityId, payload, userId, attempted}`; `SpotDao.getPendingSync()` /
  `markSynced()`.
  `UniEatsDatabase` exports its schema (Room Gradle plugin, `room { schemaDirectory(...) }`) to
  `app/schemas/…/<version>.json` (`1.json` from l07, `2.json` from l08), which are committed. Each
  schema change bumps `version` and adds an `autoMigrations` entry checked against these files.
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
  the laptop at `10.0.2.2`. It is a runtime permission, requested from `l09` on with the other
  prompts (see "Nearby + notifications"). Not verified on an API 37 device yet (the API 37
  emulator crashes on the course machine); loopback is not "local network", so `adb reverse` +
  `localhost:3000` is the fallback. The manifest also removes WorkManager's default initializer, because
  `UniEatsApplication` provides a configuration with `HiltWorkerFactory`.

Unchanged from earlier labs: Navigation 3 with the two-pane layout and deep links,
`FavouritesRepository` shared by the list and the detail screen, pull-to-refresh on every state,
Material Symbols vector drawables via `painterResource`, the TalkBack descriptions on the rating,
reviews and live badge, and the light/dark theme.

## Login and protected writes (new in l08)

- Sign in with `student@unieats.app` / `password`. The server protects every mutation from this
  tag on. `ui/login/LoginScreen.kt` scrolls when the form does not fit (landscape with the
  keyboard open) and stays centred when it does; `consumeWindowInsets(innerPadding)` keeps
  `imePadding()` from adding the navigation bar a second time.
- `data/auth/TokenStore.kt` — the JWT, display name and user id in Jetpack DataStore
  (`files/datastore/session.preferences_pb`), each value encrypted by `data/auth/KeystoreCipher.kt`
  (AES-256-GCM with a key generated in the Android Keystore; no `security-crypto`, whose
  `EncryptedSharedPreferences` is deprecated). `allowBackup="false"` keeps it out of backups.
  `session` is a `StateFlow<SessionState>`: `Loading` until DataStore has been read, then
  `SignedOut`, `Locked` (biometric unlock, below) or `SignedIn`. `AuthTokens.token()` is `suspend`
  and waits for `Loading` to end, so a request made at startup still carries the token.
- `MainActivity` keeps the system splash screen up (`installSplashScreen()` +
  `setKeepOnScreenCondition`) while the session is `Loading`, so the first frame is already the
  login screen, the unlock screen or the list.
- `data/remote/ApiClient.kt` — `authorized(token)` adds `Authorization: Bearer <jwt>` to PATCH and
  review POST (reads stay anonymous); `OutboxSync` replays with the same header.
- `di/AppModule.kt` — a 401 on a request that carried a token clears the session, and
  `ui/navigation/NavGraph.kt` puts `Login` on the back stack (also when the 1 h token expires).
  Signing in again restores `startKeys`, so a deep link that opened the app is not lost.
- Who owns a queued edit: each outbox op records the `user.id` from the `/auth/login` response.
  - A 401 (or a biometric change, below) keeps queued edits. If the same user signs in again
    they replay; if a different user signs in, `OutboxSync.discardOtherUsers()` drops the other
    user's ops and their optimistic rows before the new token is saved
    (`AuthRepository.login()`).
  - **Log out** (and "Use password instead") discards them: `AuthRepository.logout()` cancels the
    sync job, `OutboxSync.discardAll()` empties the outbox and deletes the `pendingSync` rows (the
    next load fetches the server copies), then clears the session.
  - The `userId` column is the database's first schema change: `UniEatsDatabase` is now version 2
    with `AutoMigration(from = 1, to = 2)`, which Room generates from the committed `1.json` and
    `2.json` (`ALTER TABLE outbox ADD COLUMN userId`), so installing over an l07 build keeps the
    cached spots and the outbox; ops queued there have no `userId` and replay for whoever signs in.
- **Log out** is in the ⋮ menu of the list; **Add review** is on the detail screen (online only:
  POST needs the server). The star picker in `ui/spotdetail/AddReviewDialog.kt` is a
  `selectableGroup()` of five `Role.RadioButton` items, so TalkBack reads "3 stars, selected".

## Biometric unlock (new in l08, off by default)

- Turn it on in the ⋮ menu: **Unlock with biometrics**. It is offered only on API 30+ with a
  strong (class 3) biometric enrolled (`ui/auth/BiometricPrompter.kt`, `canUseBiometricUnlock()`,
  re-checked on every resume). Off by default, so a password sign-in works as before.
- `data/auth/BiometricKeystoreCipher.kt` — a second Keystore AES-256-GCM key, created with
  `setUserAuthenticationRequired(true)`, `setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)`
  (every single use needs a prompt) and `setInvalidatedByBiometricEnrollment(true)`. The key is
  usable only through a `Cipher` that `BiometricPrompt` has authenticated, passed in as a
  `CryptoObject`. This is what makes it more than a yes/no gate in front of the app: without the
  fingerprint, the token cannot be decrypted at all.
- Turning it on: `SessionViewModel.enableBiometric()` prompts with an encrypt cipher, and
  `TokenStore.enableBiometric()` stores the token sealed with it (`jwt_biometric`) and removes the
  normally encrypted copy. Turning it off writes the normal copy back and deletes the key; no
  prompt.
- Cold start with biometrics on: the session is `Locked`, `NavGraph.kt` shows `Unlock`
  (`ui/auth/UnlockScreen.kt` + `UnlockViewModel`), which prompts once by itself ("Unlock UniEats",
  negative button "Use password"). Success opens the token into memory only, the session becomes
  `SignedIn`, and the outbox replays (`AuthRepository.completeUnlock()` requests a sync; while
  locked, `token()` is null and the worker stops with `NeedsLogin`). Cancel leaves you on the
  screen with **Unlock** and **Use password instead**, which signs out.
- A new fingerprint or face invalidates the key (`KeyPermanentlyInvalidatedException`):
  `TokenStore.unlockCipher()` clears the session and the login screen shows "Biometrics changed
  on this device. Sign in again." Queued edits stay, as after a 401.
- Log out and a 401 delete the biometric key along with the session.
- `MainActivity` is a `FragmentActivity`, because `BiometricPrompt` needs one to host its
  fragment.
- Emulator (API 30+): Settings → Security → set a PIN, then add a fingerprint, and run
  `adb -e emu finger touch 1` when the sensor asks. At the app's prompt, `adb -e emu finger touch 1`
  again. To see the enrollment-change path, add a second fingerprint and relaunch the app.

## Nearby + notifications (new in l09)

- The location icon on the list (`ic_location_on`) opens **Spots near me**: spots within 2 km,
  nearest first, with the distance. `location/LocationService.kt` wraps the Fused Location
  Provider (`requestLocationUpdates` every 5 s, `PRIORITY_HIGH_ACCURACY`: the emulator has no
  network location, so a balanced request never gets a fix there) in a `callbackFlow` whose
  `awaitClose` calls `removeLocationUpdates`; `NearbyViewModel` collects it with
  `WhileSubscribed()`, so location stops the moment you leave the screen or background the app. It loads every page once
  (`SpotRepository.refreshAllSpots()`) and filters the Room copy with `location/Geo.kt`.
- Location permission UX (`ui/nearby/NearbyScreen.kt`): an explanation and **Allow location**
  before the system dialog, a stronger rationale when `shouldShowRequestPermissionRationale` is
  true, and **Open settings** after a permanent denial. The screen re-checks the grant on every
  resume, so coming back from Settings works.
- Emulator: with the screen open, set the location to the campus, `adb emu geo fix 26.103 44.427`
  (longitude first) or Extended controls → Location → 44.427, 26.103. The default emulator
  location (California) shows "No spots within 2 km". `adb shell dumpsys location | grep -A1 "gps
  provider:"` shows the GPS request while the screen is open and `ProviderRequest[OFF]` after Back.
- The other prompts go through `ui/permissions/PermissionRequests.kt`: `RequestPermissionsOnce`
  asks for the missing ones in a single request (Android drops a second request launched while
  one is showing).
  - The list asks for `POST_NOTIFICATIONS` (Android 13+) and `ACCESS_LOCAL_NETWORK` (Android 17+)
    together. A load started before the answer has already failed, so the list reloads when local
    network access is granted.
  - The login screen asks for `ACCESS_LOCAL_NETWORK` only, since signing in is the first request
    to the laptop. The local-network request was not verified on an API 37 device (the API 37
    emulator crashes on the course machine).
- Notifications: every `spot.updated` message from the l06 WebSocket posts a local notification
  "<spot> was updated" (`notification/SpotNotificationService.kt`, channel "Spot updates");
  tapping it opens the spot via the `unieats://spots/<id>` deep link, after the unlock screen when
  biometric unlock is on. The socket runs only while the list or a spot is on screen, so
  notifications arrive while you use the app; try it with the curl PATCH below while the list is
  open. (Local notifications driven by the WebSocket only; FCM/APNs push is not used.)
- **Sort by rating** moved from the top bar into the ⋮ menu to make room for the location icon.

## Demo: offline edit → reconnect → sync

1. Open a spot, tap **Edit spot**, change the name, Save — the list shows it immediately.
2. Turn on airplane mode (or stop the server), edit again: "1 change pending" stays visible.
3. Turn the network back on (or restart the server): the banner disappears, and the server log shows
   one `PATCH` with the `Idempotency-Key` header.
4. Conflict: edit a spot offline, change the same spot with curl
   (from l08 the server needs a token: `TOKEN=$(curl -s localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"email":"student@unieats.app","password":"password"}' | jq -r .token)`,
   then `curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"name":"Server name"}'`),
   then go online — the server's newer version wins (409 → the row is replaced, the op dropped).

Kill and relaunch the app at any point: cached spots show instantly and the outbox survives.

An emulator whose network has no internet (only the laptop is reachable) never counts as online:
the network is not `VALIDATED`, so neither the connectivity trigger nor WorkManager runs the sync.
