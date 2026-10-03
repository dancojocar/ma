import 'package:dio/dio.dart';

/// Retries GETs on timeouts, dropped connections and 5xx, with exponential
/// backoff (0.5 s, 1 s, 2 s).
class RetryInterceptor extends Interceptor {
  RetryInterceptor(
    this._dio, {
    this.maxRetries = 3,
    this.baseDelay = const Duration(milliseconds: 500),
  });

  final Dio _dio;
  final int maxRetries;
  final Duration baseDelay;

  static const _attemptKey = 'retryAttempt';

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final options = err.requestOptions;
    final attempt = options.extra[_attemptKey] as int? ?? 0;
    if (attempt >= maxRetries || !_shouldRetry(err)) {
      return handler.next(err);
    }
    await Future<void>.delayed(baseDelay * (1 << attempt));
    options.extra[_attemptKey] = attempt + 1;
    try {
      handler.resolve(await _dio.fetch<dynamic>(options));
    } on DioException catch (e) {
      handler.next(e);
    }
  }

  bool _shouldRetry(DioException err) {
    if (err.requestOptions.method != 'GET') return false;
    return switch (err.type) {
      DioExceptionType.connectionTimeout ||
      DioExceptionType.sendTimeout ||
      DioExceptionType.receiveTimeout ||
      DioExceptionType.connectionError => true,
      DioExceptionType.badResponse => (err.response?.statusCode ?? 0) >= 500,
      _ => false,
    };
  }
}
