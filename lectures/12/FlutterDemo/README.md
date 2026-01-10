# Flutter Architecture Demo

A simple Counter application demonstrating strict state management using **BLoC/Cubit**.

## Architecture

This project follows a Feature-based architecture:
- `lib/app.dart`: Main App Widget and Theme configuration.
- `lib/counter/`: The Counter feature module.
  - `view/`: Contains UI Widgets (`CounterPage`, `CounterView`).
  - `cubit/`: Contains logic (`CounterCubit`).

## State Management

We use **Cubit** (from `flutter_bloc`) for state management.
- Logic is decoupled from UI.
- UI builds reactively based on State changes.

## Running

1. Ensure you have Flutter installed (`flutter doctor`).
2. Run `flutter pub get` to install dependencies.
3. Run `flutter run` to start the app.

## Project Structure
```
lib/
├── app.dart
├── counter
│   ├── cubit
│   │   └── counter_cubit.dart
│   └── view
│       └── counter_page.dart
└── main.dart
```
