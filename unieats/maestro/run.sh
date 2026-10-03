#!/usr/bin/env bash
# Runs the per-stack UniEats Maestro flows (<stack>/maestro/<stack>.yaml).
# Usage: ./maestro/run.sh [android|ios|flutter|rn|all]
# Needs: maestro CLI, the server running (cd server && npm start), and the app installed on a booted device.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v maestro >/dev/null || { echo "maestro CLI not found: curl -Ls 'https://get.maestro.mobile.dev' | bash"; exit 1; }
run() { echo; echo "=== $1: $ROOT/$1/maestro/$1.yaml ==="; maestro test "$ROOT/$1/maestro/$1.yaml"; }
case "${1:-all}" in
  android|ios|flutter|rn) run "$1" ;;
  all) for s in android ios flutter rn; do run "$s"; done ;;
  *) echo "usage: $0 [android|ios|flutter|rn|all]"; exit 1 ;;
esac
