# UniEats — Flutter

Discover campus food spots. This tag (`l03-state`) adds observable state with Riverpod 3
(no code generation): `SpotListNotifier` (`lib/providers/providers.dart`) owns one
`SpotListUiState` — the 8 canonical spots, `searchQuery`, `categoryFilter` and `favouriteIds`.
The list filters case-insensitively by the search bar and the category chips, and the heart on a
card or on the detail screen toggles a favourite. Try: search "pizza", favourite Pizza Stop, open it.
`SpotListUiState` has value equality, so a state equal to the previous one does not rebuild listeners.
`main()` builds the `ProviderScope` with `retry: (_, _) => null`: Riverpod 3 retries failing providers
by default, and retries belong in the HTTP layer instead.

## Run

Requires Flutter 3.47.6 or newer (developed on 3.50). The Android shell uses the Gradle wrapper (9.8.0) and
AGP 9.4.1 with built-in Kotlin, and builds with Android Studio 2026.1's bundled JDK 25 as is (JDK 21 works too), so there is
no `flutter config --jdk-dir` step. iOS (tested with Xcode 27, deployment target 15.0) links plugins as Swift
packages: no CocoaPods, no `pod install`.

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
