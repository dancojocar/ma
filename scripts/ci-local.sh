#!/usr/bin/env bash
# Run the Ubuntu CI jobs locally with act before pushing. macOS jobs (iOS) cannot run locally.
# Usage: scripts/ci-local.sh [server|android|flutter|rn|all]   (default: all)
# One-time setup: colima start --cpu 4 --memory 8 --disk 60 --vz-rosetta
set -euo pipefail
cd "$(dirname "$0")/.."
command -v act >/dev/null || { echo "act not installed (brew install act)"; exit 1; }
docker info >/dev/null 2>&1 || { echo "Docker is not running: colima start --cpu 4 --memory 8 --disk 60 --vz-rosetta"; exit 1; }
TOKEN=$(gh auth token 2>/dev/null || true)
run() { echo; echo "=== $1 / job $2 ==="; act push -W ".github/workflows/$1" -j "$2" ${TOKEN:+-s GITHUB_TOKEN="$TOKEN"}; }
case "${1:-all}" in
  server)  run server.yml test ;;
  android) run native.yml android ;;
  flutter) run cross.yml flutter ;;
  rn)      run cross.yml rn ;;
  all)     run server.yml test; run native.yml android; run cross.yml flutter; run cross.yml rn ;;
  *) echo "usage: $0 [server|android|flutter|rn|all]"; exit 1 ;;
esac
