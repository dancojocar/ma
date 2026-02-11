# Mobile Applications Course (MA)

This repository contains the lecture materials, source code, and demos for the Mobile Applications course. It serves as a comprehensive reference for building mobile apps using Android (Kotlin/Compose), Flutter, and iOS (Swift).

## 📂 Repository Structure

The `lectures/` directory is organized by week, covering the following topics:

| Week | Topic | Key Concepts |
|---|---|---|
| **01** | Introduction | Native/React Native/Flutter setup, Hello World apps |
| **02** | UI & Layouts | Lists, RecyclerView, ConstraintLayout, Adaptive Design |
| **03** | Navigation & Data | Fragments, Navigation Drawer, File storage, DataStore |
| **04** | Local Storage | SQLite, Room, Realm, ObjectBox, SQLDelight |
| **05** | Async Programming | Coroutines, Flow, RxJava, RxSwift, LiveData |
| **06** | State Management | Provider, Riverpod, BLoC, Hooks, Ephemeral State |
| **07** | Auth & Networking | Biometrics, CameraX, JWT Auth (Go/Rust/Flutter), Retrofit |
| **08** | Background Work | WorkManager, Sensors (Accelerometer), Alarms, Background Fetch |
| **09** | Animations | Transitions, MotionLayout, Lottie, Flutter Animations |
| **10** | Advanced UI | Maps, Accessibility, Widgets, Canvas, Localization |
| **11** | Cloud Services | Firebase (Auth/Database), AdMob, Crashlytics |
| **12** | Architecture | Clean Architecture, Jetpack Compose, KMP (Kotlin Multiplatform) |
| **13** | AI & Machine Learning | On-device ML, Face Detection, LLM Inference |
| **14** | Testing & Server | Unit/UI Testing (Espresso, Robolectric), Game Servers (Phaser) |

## 🛠 Setup & Requirements

To run the projects in this repository, you may need the following tools installed:

- **Android Studio** (for Android/Kotlin projects)
- **Xcode** (for iOS/Swift projects, requires macOS)
- **Flutter SDK**
- **Node.js** (for backend/server demos)
- **Rust/Cargo** (for Rust backend demos)
- **Go** (for Go backend demos)

## 🔐 Security & Maintenance

### Legacy Code
- The `archives.tar.gz` file contains archived legacy projects (2016-2019) that are no longer maintained.

### Dependency Management
- Please ensure you are using safe versions of dependencies.

## 🧹 Utility Scripts

- `cleanup-builds.sh`: Removes `build/` and `.gradle/` directories to free up space.
- `cleanup-credentials.sh`: Scans for and removes accidental credential commits (e.g., `google-services.json`).

## 📄 License
This project is for educational purposes. 
