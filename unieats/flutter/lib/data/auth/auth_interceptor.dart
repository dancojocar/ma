import 'package:dio/dio.dart';

import 'token_store.dart';

/// Adds `Authorization: Bearer <jwt>` to every mutation (outbox replay
/// included) and reports a rejected token through [onUnauthorized].
class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._tokens, {required this.onUnauthorized});

  final TokenStore _tokens;
  final void Function() onUnauthorized;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    if (options.method != 'GET') {
      final token = await _tokens.read();
      if (token != null) options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    final sentToken = err.requestOptions.headers.containsKey('Authorization');
    if (err.response?.statusCode == 401 && sentToken) onUnauthorized();
    handler.next(err);
  }
}
