# Profiling the Flutter app

Always measure in **profile mode** on a real device or emulator; debug mode is JIT-compiled and
several times slower, so its numbers mean nothing.

1. Start the server, then `flutter run --profile` (pick the device).
2. Open DevTools from the URL the terminal prints (or press `v`).
3. **Performance** tab → record while you fling the spot list up and down and open a spot (Hero
   transition). Each bar in the frame chart is one frame; red bars missed the frame budget
   (16 ms at 60 Hz, 8 ms at 120 Hz).
4. Click a red frame. **UI** time too long → Dart work in `build`/layout (look for rebuilds of the
   whole list: enable *Track widget rebuilds*). **Raster** time too long → painting: big images,
   clips, opacity layers, shader compilation.
5. Press `P` in the `flutter run` terminal for the on-device performance overlay (same two graphs).
6. **Memory** tab → take a snapshot after opening and closing a few detail screens; `AnimationController`s
   and stream subscriptions should not pile up (every `AnimatedEntrance` disposes its controller and timer).

What this app already does for smooth scrolling: `ListView.builder` builds only visible rows; rows
keep their state with `ValueKey(spot.id)`; `const` widgets are reused across rebuilds; the list only
rebuilds when the Drift stream emits; the entrance animation runs once per row and is skipped when
the OS asks for reduced motion. If a heavy widget repaints often, wrap it in a `RepaintBoundary` and
compare the raster times before/after.
