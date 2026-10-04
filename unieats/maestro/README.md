# UniEats Maestro flows

One flow per stack on the `l14-tests` code. Each signs in with the demo account
(`student@unieats.app` / `password`), opens the first spot, checks that the detail shows
`Reviews (n)`, taps "Describe this dish", waits for the streamed text and goes back to the list.
Biometric unlock is opt-in and off after `clearState`, so every flow starts at the password login.

| Stack | Flow | appId | Stack-specific steps |
|---|---|---|---|
| Android | `android/maestro/android.yaml` | `com.unieats.app` | Grants permissions at launch and taps Allow if a system permission dialog still shows after sign-in; logs out through ⋮ → Log out. |
| iOS | `ios/maestro/ios.yaml` | `com.unieats.app` | Clears the Keychain at launch; dismisses "Not Now" (save password) and "Allow" (notifications); waits until the button reads "Describe this dish" again, then swipes back. Does not sign out. |
| Flutter | `flutter/maestro/flutter.yaml` | `com.unieats.flutter` | Taps "Use password instead" if a kept session is locked, signs in only if "Sign In" shows, dismisses "Not Now"; waits for `describe-dish-result`; signs out through Settings → scroll → Sign out. |
| React Native | `rn/maestro/rn.yaml` | `com.unieats.rn` | Debug build, so Metro must be running; logs out through Profile → `logout-button`. |

```bash
cd server && npm start                        # terminal 1 (another port: PORT=3100 npm start)
./maestro/run.sh android                      # terminal 2, app already installed on a booted emulator
./maestro/run.sh --install android            # build and install the debug app first
UNIEATS_PORT=3100 ./maestro/run.sh --install flutter
./maestro/run.sh                              # all four
```

`--install` builds the debug app and installs it: Android with `:app:installDebug`, Flutter with
`flutter build apk --debug` + `adb install` (both on the booted Android emulator), iOS with
`xcodebuild … CODE_SIGN_IDENTITY=-` on the booted simulator. The iOS build must be signed (ad hoc
is enough): a `CODE_SIGNING_ALLOWED=NO` build has no Keychain access and login fails. For React
Native the script only prints the command to run, because the flow needs Metro running alongside:
`EXPO_PUBLIC_API_URL=http://10.0.2.2:<port>/api npx expo run:android`.

`UNIEATS_PORT` (default 3000) points the apps at a server on another port. Android and Flutter
bake the URL into the build (`-Punieats.serverHost=10.0.2.2:<port>`,
`--dart-define=API_BASE_URL=http://10.0.2.2:<port>/api`), so it needs `--install`. iOS reads
`UNIEATS_API_URL` at launch; the script sets it on the booted simulator before every iOS run:
`xcrun simctl spawn <udid> launchctl setenv UNIEATS_API_URL http://localhost:<port>/api`.

The script passes the device to Maestro explicitly, so an emulator and a simulator can be running at
the same time: `ANDROID_SERIAL` (otherwise the only adb device) for Android, Flutter and React
Native, `UNIEATS_SIMULATOR` (otherwise the first booted simulator) for iOS.

Every flow passes `maestro check-syntax` (Maestro 2.0.10). Selectors use the ids the apps set:
`login-email`, `login-password`, `login-submit` (Android, RN), `spot-list-item`,
`spot-detail-reviews`, `describe-dish-button`, then `dish-description` (Android, RN),
`describe-dish-text` (iOS) or `describe-dish-result` (Flutter), and `search-input`,
`logout-button` (RN).

In CI the `maestro-*` jobs in `.github/workflows/native.yml` and `cross.yml` run the same flows
when the workflow is started by hand with `run_maestro`; the `maestro-syntax` job in `cross.yml`
checks all four flows on every run.
