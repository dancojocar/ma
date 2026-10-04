#!/usr/bin/env bash
# Runs the per-stack UniEats Maestro flows (<stack>/maestro/<stack>.yaml).
# Usage: ./maestro/run.sh [--install] [android|ios|flutter|rn|all]
#   --install  build the debug app and install it first (android, flutter: booted Android emulator;
#              ios: booted simulator; rn: prints the command to run instead, Metro must stay up)
# Needs: maestro CLI and the server running (cd server && npm start).
# UNIEATS_PORT (default 3000) is the server's port; builds and the iOS simulator point the apps at it.
# Device: ANDROID_SERIAL (else the only adb device) for android/flutter/rn; UNIEATS_SIMULATOR (else
# the first booted simulator) for ios.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${UNIEATS_PORT:-3000}"
INSTALL=false
if [[ "${1:-}" == "--install" ]]; then INSTALL=true; shift; fi
command -v maestro >/dev/null || { echo "maestro CLI not found: curl -Ls 'https://get.maestro.mobile.dev' | bash"; exit 1; }

booted_simulator() {
  if [[ -n "${UNIEATS_SIMULATOR:-}" ]]; then echo "$UNIEATS_SIMULATOR"; return; fi
  xcrun simctl list devices booted | grep -m1 -oE '[0-9A-F]{8}-([0-9A-F]{4}-){3}[0-9A-F]{12}' \
    || { echo "no booted iOS simulator" >&2; exit 1; }
}

device() {
  if [[ "$1" == ios ]]; then booted_simulator; else adb get-serialno; fi
}

install_android() {
  (cd "$ROOT/android" && ./gradlew :app:installDebug -Punieats.serverHost="10.0.2.2:$PORT" --console=plain)
}

install_flutter() {
  (cd "$ROOT/flutter" \
    && flutter build apk --debug --dart-define=API_BASE_URL="http://10.0.2.2:$PORT/api" \
    && adb install -r build/app/outputs/flutter-apk/app-debug.apk)
}

# CODE_SIGNING_ALLOWED=NO would leave the app without Keychain access, so login would fail.
install_ios() {
  local udid derived
  udid="$(booted_simulator)"
  derived="$(mktemp -d)"
  xcodebuild -project "$ROOT/ios/UniEats.xcodeproj" -scheme UniEats \
    -destination "platform=iOS Simulator,id=$udid" -derivedDataPath "$derived" \
    build CODE_SIGN_IDENTITY=- -quiet
  xcrun simctl install "$udid" "$derived/Build/Products/Debug-iphonesimulator/UniEats.app"
  rm -rf "$derived"
}

install_rn() {
  echo "rn: build, install and keep Metro running in another terminal first:"
  echo "    cd $ROOT/rn && EXPO_PUBLIC_API_URL=http://10.0.2.2:$PORT/api npx expo run:android"
}

run() {
  local id
  id="$(device "$1")"
  if $INSTALL; then "install_$1"; fi
  if [[ "$1" == ios ]]; then
    xcrun simctl spawn "$id" launchctl setenv UNIEATS_API_URL "http://localhost:$PORT/api"
  fi
  echo; echo "=== $1 on $id: $ROOT/$1/maestro/$1.yaml ==="
  maestro --device "$id" test "$ROOT/$1/maestro/$1.yaml"
}

case "${1:-all}" in
  android|ios|flutter|rn) run "$1" ;;
  all) for s in android ios flutter rn; do run "$s"; done ;;
  *) echo "usage: $0 [--install] [android|ios|flutter|rn|all]"; exit 1 ;;
esac
