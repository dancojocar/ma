import 'dart:convert';

import 'package:unieats_data/unieats_data.dart';
import '../network/api_client.dart';
import 'token_store.dart';

class TokenAuthRepository implements AuthRepository {
  TokenAuthRepository(this._api, this._tokens);

  final ApiClient _api;
  final TokenStore _tokens;

  @override
  Future<User> login(String email, String password) async {
    final result = await _api.login(email, password);
    await _tokens.write(
      result.token,
      userJson: jsonEncode(result.user.toJson()),
    );
    return result.user;
  }

  @override
  Future<void> logout() => _tokens.clear();

  @override
  /// The signed-in user if a stored token has not expired yet, checked
  /// locally from the token's `exp` claim. The server still verifies the
  /// signature on every mutation.
  Future<User?> restoreSession() async {
    final token = await _tokens.read();
    final userJson = await _tokens.readUser();
    final exp = token == null ? null : _claims(token)?['exp'];
    final expired =
        exp is! int ||
        DateTime.fromMillisecondsSinceEpoch(
          exp * 1000,
        ).isBefore(DateTime.now());
    if (expired || userJson == null) {
      await _tokens.clear();
      return null;
    }
    return User.fromJson(jsonDecode(userJson) as Map<String, dynamic>);
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
