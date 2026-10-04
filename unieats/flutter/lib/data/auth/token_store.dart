import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:unieats_data/unieats_data.dart';

/// The JWT lives in the Keychain (iOS, readable after the first unlock, never
/// migrated to another device) and, on Android, in SharedPreferences encrypted
/// with AES-GCM under a key wrapped by the Android Keystore.
///
/// With biometric unlock on, the JWT moves to a second store whose key only
/// opens after a strong biometric check: on Android a Keystore AES-GCM key that
/// needs user authentication and is invalidated by a new enrollment, unwrapped
/// through `BiometricPrompt`; on iOS a Keychain item with `biometryCurrentSet`.
class TokenStore {
  TokenStore([FlutterSecureStorage? storage, FlutterSecureStorage? biometric])
    : _storage =
          storage ??
          const FlutterSecureStorage(
            iOptions: IOSOptions(
              accessibility: KeychainAccessibility.first_unlock_this_device,
            ),
          ),
      _biometricStorage =
          biometric ??
          const FlutterSecureStorage(
            aOptions: AndroidOptions.biometric(
              enforceBiometrics: true,
              biometricType: AndroidBiometricType.strongBiometricOnly,
              requireBiometricsPerOperation: true,
              storageNamespace: 'unieats_biometric',
              biometricPromptTitle: 'Unlock UniEats',
              biometricPromptNegativeButton: 'Use password',
            ),
            iOptions: IOSOptions(
              accountName: 'unieats_biometric',
              accessibility: KeychainAccessibility.passcode,
              accessControlFlags: [AccessControlFlag.biometryCurrentSet],
            ),
          );

  static const _tokenKey = 'jwt';
  static const _userKey = 'user';
  static const _biometricFlagKey = 'biometric';

  // A key name of its own as well: in tests both stores share one mock map.
  static const _biometricTokenKey = 'jwt_biometric';

  final FlutterSecureStorage _storage;
  final FlutterSecureStorage _biometricStorage;

  /// The biometric copy after [unlockBiometric] or [enableBiometric]. The
  /// interceptor reads the token for every mutation and must never prompt.
  String? _unlockedToken;

  Future<String?> read() async =>
      _unlockedToken ?? await _storage.read(key: _tokenKey);

  Future<String?> readUser() => _storage.read(key: _userKey);

  Future<void> write(String token, {required String userJson}) async {
    await _storage.write(key: _tokenKey, value: token);
    await _storage.write(key: _userKey, value: userJson);
  }

  Future<bool> biometricEnabled() async =>
      await _storage.read(key: _biometricFlagKey) == 'on';

  /// Moves the JWT into the biometric store. Android prompts here, because
  /// the key only encrypts after a biometric check.
  Future<void> enableBiometric() async {
    final token = await read();
    if (token == null) throw StateError('Not signed in');
    try {
      await _biometricStorage.write(key: _biometricTokenKey, value: token);
    } on PlatformException catch (e) {
      if (_isCancel(e)) throw const BiometricCancelled();
      rethrow;
    }
    _unlockedToken = token;
    await _storage.write(key: _biometricFlagKey, value: 'on');
    await _storage.delete(key: _tokenKey);
  }

  Future<void> disableBiometric() async {
    final token = _unlockedToken;
    if (token != null) await _storage.write(key: _tokenKey, value: token);
    await _storage.delete(key: _biometricFlagKey);
    await _deleteBiometricToken();
  }

  /// Shows the biometric prompt and returns the stored JWT.
  ///
  /// Throws [BiometricCancelled] when the user backs out and
  /// [BiometricInvalidated] when the key no longer opens: after a new
  /// enrollment Android wipes the store and iOS no longer finds the item.
  Future<String> unlockBiometric() async {
    final String? token;
    try {
      token = await _biometricStorage.read(key: _biometricTokenKey);
    } on PlatformException catch (e) {
      if (_isCancel(e)) throw const BiometricCancelled();
      throw const BiometricInvalidated();
    }
    if (token == null) throw const BiometricInvalidated();
    return _unlockedToken = token;
  }

  Future<void> clear() async {
    final biometric = await biometricEnabled();
    _unlockedToken = null;
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _userKey);
    await _storage.delete(key: _biometricFlagKey);
    if (biometric) await _deleteBiometricToken();
  }

  Future<void> _deleteBiometricToken() async {
    try {
      await _biometricStorage.delete(key: _biometricTokenKey);
    } on PlatformException {
      // Android will not open the store once no biometric is enrolled. The key
      // is invalid by then, so what is left behind cannot be decrypted.
    }
  }

  /// iOS: errSecUserCanceled. Android BiometricPrompt errors: canceled (5),
  /// lockout (7, 9), user canceled (10), negative button (13).
  static bool _isCancel(PlatformException e) =>
      e.details == -128 ||
      RegExp(r'Biometric authentication error \[(5|7|9|10|13)\]')
          .hasMatch(e.message ?? '');
}
