# Bird Gallery - Kotlin Multiplatform Project

A clean, modern Kotlin Multiplatform (KMP) bird image gallery application that replaces the `my-bird-app-main` project with improved architecture and maintainability.

## Features

- **Cross-platform**: Shared business logic for Android and iOS
- **Bird Image Gallery**: Browse bird images by category
- **HTTP Networking**: Fetches bird images from a remote API
- **Modern UI**: Material 3 Design on Android with Jetpack Compose
- **Image Loading**: Efficient image loading with Coil

## Architecture

### Shared Module
- **Platform**: Platform detection (Android/iOS)
- **Models**: `BirdImage` and `BirdsUiState` data classes
- **Repository**: `BirdsRepository` for API calls using Ktor
- **ViewModel**: `BirdsViewModel` for state management

### Android App
- **Jetpack Compose UI**: Modern declarative UI
- **Material 3**: Latest Material Design components
- **Coil**: Image loading library
- **Category Selection**: Buttons to filter images by category
- **Grid Layout**: 2-column grid for images

## Technology Stack

- **Kotlin**: 1.9.20
- **Ktor**: HTTP client for networking
- **Kotlinx Serialization**: JSON parsing
- **Jetpack Compose**: Android UI
- **Material 3**: UI components
- **Coil**: Image loading

## Building

```bash
# Build all targets
./gradlew assemble

# Build Android app
./gradlew :androidApp:assemble

# Build shared library
./gradlew :shared:assemble
```

## Running

### Android
1. Open project in Android Studio
2. Select `androidApp` run configuration
3. Run on emulator or device

### iOS
The iOS app UI is not yet implemented, but the shared module builds iOS frameworks that can be integrated into a native iOS app.

## Differences from my-bird-app-main

1. **Simpler Dependencies**: Removed heavy dependencies like Moko MVVM and Kamel
2. **Standard Architecture**: Uses simple StateFlow instead of complex MVVM framework
3. **Material 3**: Updated to latest Material Design
4. **Better Maintainability**: Cleaner package structure and naming
5. **Working Build**: All configurations tested and verified

## Project Structure

```
bird-gallery/
├── shared/               # Shared Kotlin Multiplatform code
│   ├── src/
│   │   ├── commonMain/   # Platform-independent code
│   │   ├── androidMain/  # Android-specific code
│   │   └── iosMain/      # iOS-specific code
│   └── build.gradle.kts
├── androidApp/           # Android application
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/birdgallery/android/
│   │       └── MainActivity.kt
│   └── build.gradle.kts
├── build.gradle.kts      # Root build configuration
├── settings.gradle.kts   # Project settings
└── gradle.properties     # Gradle properties
```

## API

The app fetches bird images from:
```
https://sebastianaigner.github.io/demo-image-api/pictures.json
```

Each bird image includes:
- `author`: Image credits
- `category`: Bird category/type
- `path`: Relative path to the image

## License

This is a demonstration project for educational purposes.
