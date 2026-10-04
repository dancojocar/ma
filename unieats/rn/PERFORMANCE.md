# Profiling the React Native app (Expo SDK 57, RN 0.86, Hermes)

Profile a release-like JS bundle: `npx expo start --no-dev --minify`, or a release build
(`npx expo run:android --variant release`). Dev mode is several times slower and misleads.

1. **Perf Monitor** — dev menu (`m` in the Metro terminal, or shake) → "Perf Monitor". Watch the
   UI and JS frame rates while you scroll the Spots list and swipe a row away. The swipe and the
   `FadeInDown` entrance run as worklets on the UI thread (Reanimated 4 + `react-native-worklets`), so
   the UI FPS should stay at 60 even when JS is busy (pull-to-refresh parsing a page).
2. **React Native DevTools** — press `j` in the Metro terminal. *Profiler* tab → record, scroll the
   list, stop: the flame chart shows which components re-rendered and why ("Record why each
   component rendered" in the settings). `SpotCard` should not re-render for rows whose spot did
   not change.
3. **Hermes sampling profiler** — DevTools *Performance* tab (or dev menu → "Toggle Sampling
   Profiler", then `npx react-native profile-hermes`) for JS CPU time, e.g. the SQLite reads in
   `useLocalSpots`.
4. **Native side** — Android Studio Profiler or Perfetto (`adb shell perfetto` / ui.perfetto.dev)
   for frame timing and main-thread jank; Xcode Instruments (Time Profiler, Animation Hitches) on iOS.

Flipper is not supported on React Native 0.86; its features moved into React Native DevTools.
