#!/bin/bash
set -e

# Define project root relative to this script
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "📱 Building and installing app..."
"$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" :app:installDebug

echo "🎭 Running Maestro tests..."
# Check if maestro is installed
if ! command -v maestro &> /dev/null; then
    echo "Error: maestro could not be found. Please install it with: curl -Ls \"https://get.maestro.mobile.dev\" | bash"
    exit 1
fi

maestro test "$PROJECT_ROOT/maestro/flow.yaml"
