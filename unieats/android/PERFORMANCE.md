# Profiling UniEats on Android

Profile a **release-like** build on a real device when you can: debug builds run Compose without
optimisations and look slower than they are.

## 1. Is it janky at all? — on-screen bars (30 s)
Developer options → *Profile HWUI rendering* → *On screen as bars*. Scroll the spot list. Every bar
above the green 16 ms line is a dropped frame at 60 Hz. No tall bars → stop here.

## 2. What recomposes too often? — Layout Inspector
Run the app, then *View → Tool Windows → Layout Inspector*, enable **Show Recomposition Counts**.
Scroll the list and type in the search bar:
- rows should not recompose while you type unless their data changed (`key = { it.id }` and
  stable `Spot` data classes let Compose skip them);
- toggle Sort in the ⋮ menu: rows move (`animateItem`) instead of recomposing in place.
Try deleting `key = { it.id }` in `SpotListScreen.kt` and watch the counts jump.

## 3. Which thread misses the frame budget? — Perfetto
*Profiler → CPU → System Trace* (or record on device: Developer options → System tracing) and
open the trace at <https://ui.perfetto.dev>. Look at the `main` and `RenderThread` slices for
frames over 16 ms; Compose adds `Recomposer:recompose` and per-composable sections when
composition tracing is enabled.

## 4. Leaks and GC pauses — Memory Profiler
*Profiler → Memory*. Navigate list → detail → back three times, *Force GC*, then *Capture heap
dump*: there should be no `SpotDetailViewModel` or `MainActivity` instances left over.

## Animations in this tag
- `SpotListScreen.kt` — `Modifier.animateItem()` on every row (reorder / insert / remove).
- `spotdetail/AboutCard.kt` — tap the About card: `animateContentSize()` with a spring.
- `navigation/NavGraph.kt` — slide + fade `enterTransition` / `exitTransition` /
  `popEnterTransition` / `popExitTransition` on the `NavHost`.
