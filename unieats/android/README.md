# UniEats Android — tag `l02-ui`

A static list of the 8 campus food spots and a detail screen, all from hard-coded data
(`data/model/SeedData.kt`, the same 8 spots the server seeds). No network, no ViewModel yet.

## Run

```bash
./gradlew -p unieats/android :app:installDebug
```

or open `unieats/android` in Android Studio and press Run.

## What to look at

- `ui/spotlist/SpotListScreen.kt` — `LazyColumn` with `items(spots, key = { it.id })` and
  `Modifier.animateItem()` on every row. Tap **Sort** in the top bar: the rows slide to their new
  positions. Delete `key = { it.id }` and tap Sort again: the rows just flash, because Compose no
  longer knows which row is which.
- `ui/spotlist/SpotCard.kt` — one row, a stateless composable (`spot` + `onClick` in, UI out).
- `ui/spotdetail/SpotDetailScreen.kt` — detail with photo, rating, description and "Reviews (n)".
- Icons are Material Symbols vector drawables in `res/drawable` (`ic_arrow_back` is auto-mirrored
  for right-to-left), drawn with `painterResource`, so no icon library is needed. The status labels
  on the detail screen are plain `Surface`s rather than chips, so TalkBack does not call them
  buttons; the rating line and each review carry a spoken description instead of reading "★".
- `MainActivity.kt` — list → detail is a `rememberSaveable` selected id (real navigation arrives
  at `l04-nav`). The system Back button returns to the list.

The window theme has a `values-night` variant and `UniEatsTheme` uses dynamic colour on Android 12+,
so the app follows the system light/dark setting from the first frame.

Photos are loaded from `photoUrl` (picsum.photos) with Coil, so the emulator needs internet for
images; everything else works offline.
