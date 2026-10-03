# UniEats — Flutter

Discover campus food spots. This tag (`l02-ui`) shows the 8 canonical campus spots
(hard-coded in `lib/data/seed_data.dart`) in a `ListView.builder` of `SpotCard`s; tapping a
card pushes `SpotDetailScreen` with that spot.

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
