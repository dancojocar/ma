# UniEats – React Native (Expo SDK 57)

Tag `l11-cloud`: Profile → Settings has **Remote config** — the flag `show_new_rating_ui` (default
`false`) from `GET /api/config`, applied by "Fetch & activate" (`src/cloud/featureFlags.ts`); when true,
ratings render as the new green rating pill (`RatingBadge`). Flip it for the class with
`curl -X POST localhost:3000/api/config -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"flags":{"show_new_rating_ui":true}}'`.
**Crash reporting** goes through a `CrashReporter` interface (`src/cloud/crashReporting.ts`; the default
backend logs, a Sentry/Crashlytics backend would plug in there) behind an opt-in consent switch stored in
SQLite; uncaught JS errors and the root `ErrorBoundary` report through it, and a dev-only "Test crash" button
calls `crashReporter.recordError()`. No Firebase dependency.

Kept from l10: `FadeInDown` row entrance and swipe-to-hide (`PERFORMANCE.md` for profiling).
Kept from l09: the Nearby tab (2 km, `expo-location` watch only while focused) and a local notification
"<spot> was updated" from live `spot.updated` events (`expo-notifications`).

Emulator location (the seed is in Bucharest): `adb emu geo fix 26.1025 44.4268`; iOS simulator:
Features → Location → Custom Location 44.4268, 26.1025. Trigger a notification:
`curl -X PATCH localhost:3000/api/spots/spot-3 -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"rating":4.9}'`
(get `$TOKEN` from `POST /api/auth/login`).

Kept from l08: login (`student@unieats.app` / `password`), JWT in memory + `expo-secure-store`, Bearer on
every mutation and outbox replay, 401 → login, add-review, profile / sign out.
Everything from l07 is kept: SQLite as the only UI source, outbox + NetInfo replay with
`Idempotency-Key`, 409 last-write-wins, "N changes pending", live updates, infinite scroll, pull-to-refresh,
search / category filter / favourites.

Demo: edit a spot, toggle airplane mode on the emulator, edit another, turn it off → both PATCHes arrive.

Start the server first: `cd unieats/server && npm ci && npm start` (port 3000).

## Run

```bash
cd unieats/rn
npm ci
npx expo run:android  # dev build on the emulator (Expo Go cannot load this tag on Android, see below)
```

The project `.npmrc` sets `legacy-peer-deps=false`, so `npm ci` installs the same complete tree on npm 10 and 11 even if your global `~/.npmrc` enables legacy peer deps.

Expo Go cannot run this tag on Android: the app imports expo-notifications (from l09), and in Expo Go for SDK 57
that import throws on Android (Expo Go dropped push support there in SDK 53, and the module now refuses to load),
even though UniEats only schedules local notifications. Use a dev build: `npx expo run:android` (Android Studio's
bundled Java 25 is fine: the `plugins/withPrefabNativeAccess.js` config plugin makes the generated
`android/gradlew` pass the JDK 24+ native-access flag that AGP 8.12's Prefab step needs). It also applies the
`app.json` plugin settings (location rationale text, notification colour) and the `unieats://` / https deep links.

Checks: `npx tsc --noEmit` and `npx expo-doctor`.

## API base URL

Default: `http://10.0.2.2:3000/api` on the Android emulator, `http://localhost:3000/api` on the iOS
simulator (`src/api/config.ts`, chosen by `Platform.OS`). On a physical device set your laptop's LAN IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.23:3000/api npx expo start
```

App id: `com.unieats.rn` (Android package and iOS bundle id), so it installs next to the
native and Flutter UniEats apps.
