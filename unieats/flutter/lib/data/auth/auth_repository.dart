import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:unieats_data/unieats_data.dart';

import '../network/api_client.dart';
import 'token_store.dart';

class TokenAuthRepository implements AuthRepository {
  TokenAuthRepository(this._api, this._tokens, this._spots, this._localAuth);

  final ApiClient _api;
  final TokenStore _tokens;
  final SpotRepository _spots;
  final LocalAuthentication _localAuth;

  /// Edits another account left queued when its session expired are dropped
  /// before the new token is stored, so they never replay under this account.
  @override
  Future<User> login(String email, String password) async {
    final result = await _api.login(email, password);
    await _spots.discardChangesNotBy(result.user.id);
    await _tokens.write(
      result.token,
      userJson: jsonEncode(result.user.toJson()),
    );
    return result.user;
  }

  @override
  Future<void> logout() async {
    await _tokens.clear();
    await _spots.discardLocalChanges();
  }

  @override
  Future<void> endSession() => _tokens.clear();

  /// With biometric unlock on the token is locked, so its expiry is checked
  /// in [unlock].
  @override
  Future<Session> restoreSession() async {
    final user = await _storedUser();
    if (user == null) {
      await _tokens.clear();
      return const SignedOut();
    }
    if (await _tokens.biometricEnabled()) return Locked(user);
    final token = await _tokens.read();
    if (token == null || _isExpired(token)) {
      await _tokens.clear();
      return const SignedOut();
    }
    return SignedIn(user);
  }

  @override
  Future<User> unlock() async {
    final user = await _storedUser();
    try {
      if (user == null) throw const BiometricInvalidated();
      final token = await _tokens.unlockBiometric();
      if (_isExpired(token)) throw const SessionExpired();
      return user;
    } on BiometricCancelled {
      rethrow;
    } on Exception {
      await endSession();
      rethrow;
    }
  }

  @override
  Future<bool> biometricsAvailable() async {
    try {
      if (!await _localAuth.canCheckBiometrics) return false;
      final types = await _localAuth.getAvailableBiometrics();
      return types.contains(BiometricType.strong) ||
          types.contains(BiometricType.face) ||
          types.contains(BiometricType.fingerprint);
    } on LocalAuthException {
      return false;
    } on PlatformException {
      return false;
    }
  }

  @override
  Future<bool> biometricEnabled() => _tokens.biometricEnabled();

  @override
  Future<void> enableBiometric() => _tokens.enableBiometric();

  @override
  Future<void> disableBiometric() => _tokens.disableBiometric();

  Future<User?> _storedUser() async {
    final json = await _tokens.readUser();
    if (json == null) return null;
    return User.fromJson(jsonDecode(json) as Map<String, dynamic>);
  }

  /// Checked locally from the token's `exp` claim. The server still verifies
  /// the signature on every mutation.
  bool _isExpired(String token) {
    final exp = _claims(token)?['exp'];
    return exp is! int ||
        DateTime.fromMillisecondsSinceEpoch(exp * 1000)
            .isBefore(DateTime.now());
  }

  Map<String, dynamic>? _claims(String token) {
    final parts = token.split('.');
    if (parts.length != 3) return null;
    try {
      final json = utf8.decode(base64Url.decode(base64Url.normalize(parts[1])));
      return jsonDecode(json) as Map<String, dynamic>;
    } on FormatException {
      return null;
    }
  }
}
