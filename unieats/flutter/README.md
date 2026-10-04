# UniEats — Flutter

Discover campus food spots. This tag (`l01-hello`) is the bare shell: one screen,
"UniEats / Find your next campus meal."

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
