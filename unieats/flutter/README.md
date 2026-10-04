# UniEats — Flutter

Discover campus food spots. This tag (`l04-nav`) adds go_router (`lib/app_router.dart`):
the list pushes `/spots/<id>` and the detail screen looks the spot up by ID. Search, category
filter and favourites from l03 are kept (`lib/providers/providers.dart`).

Deep links open the detail screen directly, with the list underneath on the back stack:

```bash
adb shell am start -W -a android.intent.action.VIEW -d "unieats://spots/spot-3" com.unieats.flutter
adb shell am start -W -a android.intent.action.VIEW -d "https://unieats.app/spots/spot-3" com.unieats.flutter
xcrun simctl openurl booted "unieats://spots/spot-3"
```

Android declares both intent filters in `android/app/src/main/AndroidManifest.xml`; iOS registers
the `unieats` scheme in `ios/Runner/Info.plist` (`CFBundleURLTypes`). The https form is not a
verified App Link / Universal Link (that needs files hosted on unieats.app), so pass the package
name as above.

## Run

Requires Flutter 3.47.6 or newer. The Android shell uses the Gradle wrapper (9.8.0) and AGP 9.4.1 with
built-in Kotlin, and builds with Android Studio 2026.1's bundled JDK 25 as is (JDK 21 works too), so there is
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
