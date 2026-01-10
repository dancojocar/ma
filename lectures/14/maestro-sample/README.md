# Maestro Sample Project

This project simulates a "Task Manager" app to demonstrate **Maestro** UI testing.

## Prerequisites
1.  Install Maestro:
    ```bash
    curl -Ls "https://get.maestro.mobile.dev" | bash
    ```

## Running the Sample
1.  Install the app on your connected device/emulator:
    ```bash
    ./gradlew :maestro-sample:app:installDebug
    ```
2.  Run the Maestro flow:
    ```bash
    maestro test maestro-sample/maestro/flow.yaml
    ```

## What it tests
- Launching the app
- Text input ("Buy Milk")
- Tapping buttons
- Verifying UI visibility
