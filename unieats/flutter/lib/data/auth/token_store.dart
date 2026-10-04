import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// The JWT lives in the Keychain (iOS, readable after the first unlock, never
/// migrated to another device) and, on Android, in SharedPreferences encrypted
/// with AES-GCM under a key wrapped by the Android Keystore.
class TokenStore {
  TokenStore([FlutterSecureStorage? storage])
    : _storage =
          storage ??
          const FlutterSecureStorage(
            iOptions: IOSOptions(
              accessibility: KeychainAccessibility.first_unlock_this_device,
            ),
          );

  static const _tokenKey = 'jwt';
  static const _userKey = 'user';

  final FlutterSecureStorage _storage;

  Future<String?> read() => _storage.read(key: _tokenKey);

  Future<String?> readUser() => _storage.read(key: _userKey);

  Future<void> write(String token, {required String userJson}) async {
    await _storage.write(key: _tokenKey, value: token);
    await _storage.write(key: _userKey, value: userJson);
  }

  Future<void> clear() async {
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _userKey);
  }
}
