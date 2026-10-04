# Profiling UniEats on iOS (l10)

Profile a **Release** build on a real device when you can; the simulator uses the Mac's GPU/CPU.

1. **Product › Profile (⌘I)** builds Release and opens Instruments.
2. **SwiftUI** template → *View Body* and *View Properties* lanes: scroll the spot list and type in
   search. A body that re-evaluates on every keystroke is the thing to fix (the search is
   debounced 300 ms in `SpotListViewModel`; SwiftData `@Query` re-filters locally).
3. **Animation Hitches** template: open/close a spot (zoom transition) and scroll fast;
   hitches show as red markers with the frame that missed its deadline.
4. **Time Profiler**: select a spike on the main thread, *Invert Call Tree* + *Hide System
   Libraries* to find our own hot function.
5. **Allocations** / **Leaks**: push and pop the detail screen ten times; *Mark Generation*
   between rounds — the generation growth should go back to ~0 (a growing `SpotDetailViewModel`
   count means a retain cycle).
6. Quick check without Instruments: put `let _ = Self._printChanges()` at the top of a `body`
   to log which property made SwiftUI re-render it (remove it afterwards).

The list animations in this tag: `.animation(.easeInOut(duration: 0.25), value: spots.map(\.id))`
on the list, `.transition(.move(edge: .leading).combined(with: .opacity))` on rows, and the
row photo → detail zoom (`matchedTransitionSource` + `.navigationTransition(.zoom)`).
