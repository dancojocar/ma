# UniEats iOS — l01-hello

A single SwiftUI screen: "UniEats / Find your next campus meal."

## Run

1. `open UniEats.xcodeproj` (Xcode 16 or later).
2. Pick an iPhone simulator and press Run (Cmd+R).

From the command line:

```bash
xcodebuild -project UniEats.xcodeproj -scheme UniEats \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

The `UniEats/` folder is a synchronized group: any Swift file added to it is compiled
without editing the project file. Bundle id: `com.unieats.app`.
