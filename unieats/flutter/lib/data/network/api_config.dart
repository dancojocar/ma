import 'dart:io' show Platform;

import 'package:flutter/foundation.dart' show kIsWeb;

const _apiBaseUrlOverride = String.fromEnvironment('API_BASE_URL');

/// `flutter run --dart-define=API_BASE_URL=http://192.168.1.20:3000/api`
/// overrides the default. The Android emulator reaches the host machine via
/// 10.0.2.2; the iOS simulator shares the host's localhost.
String get apiBaseUrl {
  if (_apiBaseUrlOverride.isNotEmpty) return _apiBaseUrlOverride;
  final host = !kIsWeb && Platform.isAndroid ? '10.0.2.2' : 'localhost';
  return 'http://$host:3000/api';
}

/// The WebSocket lives at the server root (`ws://<host>:3000/live`), not under `/api`.
Uri get liveUrl {
  final api = Uri.parse(apiBaseUrl);
  return api.replace(
    scheme: api.scheme == 'https' ? 'wss' : 'ws',
    path: '/live',
  );
}
