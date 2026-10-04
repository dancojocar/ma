# UniEats Maestro flows

One flow per stack, each walking the same journey on the `l14-tests` code:
list → first spot → detail shows `Reviews (` → "Describe this dish" → back.

| Stack | Flow | appId |
|---|---|---|
| Android | `android/maestro/android.yaml` | `com.unieats.app` |
| iOS | `ios/maestro/ios.yaml` | `com.unieats.app` |
| Flutter | `flutter/maestro/flutter.yaml` | `com.unieats.flutter` |
| React Native | `rn/maestro/rn.yaml` | `com.unieats.rn` |

```bash
cd server && npm start            # terminal 1
./maestro/run.sh android          # terminal 2, with the app installed on a booted emulator
./maestro/run.sh                  # all four
```

Every flow passes `maestro check-syntax` (Maestro 2.0.10). Selectors use the ids the apps set:
`spot-list-item`, `spot-detail-reviews`, `describe-dish-button`.
