# UniEats — Flutter

Discover campus food spots. This tag (`l03-state`) adds observable state with Riverpod
(no code generation): `SpotListNotifier` (`lib/providers/providers.dart`) owns one
`SpotListUiState` — the 8 canonical spots, `searchQuery`, `categoryFilter` and `favouriteIds`.
The list filters case-insensitively by the search bar and the category chips, and the heart on a
card or on the detail screen toggles a favourite. Try: search "pizza", favourite Pizza Stop, open it.

## Run

```bash
flutter pub get
flutter run            # pick an Android emulator or iOS simulator
```

Android application id and iOS bundle id: `com.unieats.flutter`.

## Check

```bash
flutter analyze
flutter build apk --debug
```
