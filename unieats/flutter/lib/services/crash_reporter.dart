import 'dart:developer' as developer;

import 'package:shared_preferences/shared_preferences.dart';

/// Where errors go. The course app logs them; a release app would plug in
/// Crashlytics or Sentry behind the same interface.
abstract interface class CrashReporter {
  Future<void> recordError(
    Object error,
    StackTrace stack, {
    bool fatal = false,
  });
}

class LoggingCrashReporter implements CrashReporter {
  const LoggingCrashReporter();

  @override
  Future<void> recordError(
    Object error,
    StackTrace stack, {
    bool fatal = false,
  }) async => developer.log(
    fatal ? 'FATAL' : 'non-fatal',
    name: 'crash',
    error: error,
    stackTrace: stack,
  );
}

/// Forwards nothing until the user has opted in (stored on the device).
class ConsentGatedCrashReporter implements CrashReporter {
  ConsentGatedCrashReporter(this._delegate, [SharedPreferencesAsync? prefs])
    : _prefs = prefs ?? SharedPreferencesAsync();

  static const _consentKey = 'crash_reporting_consent';

  final CrashReporter _delegate;
  final SharedPreferencesAsync _prefs;

  Future<bool> hasConsent() async => await _prefs.getBool(_consentKey) ?? false;

  Future<void> setConsent(bool granted) => _prefs.setBool(_consentKey, granted);

  @override
  Future<void> recordError(
    Object error,
    StackTrace stack, {
    bool fatal = false,
  }) async {
    if (await hasConsent()) {
      await _delegate.recordError(error, stack, fatal: fatal);
    }
  }
}
