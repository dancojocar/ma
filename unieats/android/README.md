# UniEats Android — tag `l01-hello`

The bare app shell: one Compose screen, "UniEats / Find your next campus meal."

## Run

1. Open `unieats/android` in Android Studio 2026.2 or newer (Gradle runs on its bundled JDK; nothing to configure).
2. Start an emulator or plug in a phone with USB debugging on.
3. Press Run, or from the repo root:

```bash
./gradlew -p unieats/android :app:installDebug
```

The application id is `com.unieats.app` (Settings → Apps shows it).

## Notes

- No server is needed at this tag.
- `res/xml/network_security_config.xml` already allows plain http only to `10.0.2.2` (the
  emulator's alias for your laptop) and `localhost`, which is what the later tags use to reach the
  dev server. Every other host must use https.
