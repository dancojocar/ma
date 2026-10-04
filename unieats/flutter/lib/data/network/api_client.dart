import 'dart:convert';
import 'dart:io' show IOException;

import 'package:dio/dio.dart';
import 'package:unieats_data/unieats_data.dart';

import 'app_error.dart';
import 'retry_interceptor.dart';
import 'sse.dart';

Dio createDio(String baseUrl) {
  final dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 15),
      sendTimeout: const Duration(seconds: 15),
      contentType: Headers.jsonContentType,
    ),
  );
  dio.interceptors.add(RetryInterceptor(dio));
  return dio;
}

/// Thin typed wrapper over the UniEats REST API. Every failure surfaces as an
/// [AppError].
class ApiClient {
  ApiClient(this._dio);

  final Dio _dio;

  Future<SpotsPage> fetchSpots({
    int page = 1,
    int limit = 20,
    String? query,
    String? category,
  }) => _send(
    () => _dio.get<Map<String, dynamic>>(
      '/spots',
      queryParameters: {
        'page': page,
        'limit': limit,
        if (query != null && query.isNotEmpty) 'q': query,
        'category': ?category,
      },
    ),
    SpotsPage.fromJson,
  );

  Future<Spot> fetchSpot(String id) =>
      _send(() => _dio.get<Map<String, dynamic>>('/spots/$id'), Spot.fromJson);

  Future<List<Review>> fetchReviews(String spotId) => _send(
    () => _dio.get<List<dynamic>>('/spots/$spotId/reviews'),
    (list) =>
        list.map((e) => Review.fromJson(e as Map<String, dynamic>)).toList(),
  );

  Future<({String token, User user})> login(String email, String password) =>
      _send(
        () => _dio.post<Map<String, dynamic>>(
          '/auth/login',
          data: {'email': email, 'password': password},
        ),
        (json) => (
          token: json['token'] as String,
          user: User.fromJson(json['user'] as Map<String, dynamic>),
        ),
      );

  Future<Review> createReview(
    String spotId, {
    required int stars,
    required String text,
    required String idempotencyKey,
  }) => _send(
    () => _dio.post<Map<String, dynamic>>(
      '/spots/$spotId/reviews',
      data: {'stars': stars, 'text': text},
      options: Options(headers: {'Idempotency-Key': idempotencyKey}),
    ),
    Review.fromJson,
  );

  /// `POST /ai/describe` answers with Server-Sent Events; each `data`
  /// frame carries `{"delta": "..."}`, emitted here as soon as it arrives.
  /// The stream ends quietly when [cancelToken] is cancelled.
  Stream<String> describeSpot(Spot spot, {CancelToken? cancelToken}) async* {
    final Response<ResponseBody> response;
    try {
      response = await _dio.post<ResponseBody>(
        '/ai/describe',
        data: {'spot': spot.toJson()},
        cancelToken: cancelToken,
        options: Options(
          responseType: ResponseType.stream,
          receiveTimeout: const Duration(seconds: 60),
          headers: {'Accept': 'text/event-stream'},
        ),
      );
    } on DioException catch (e) {
      if (CancelToken.isCancel(e)) return;
      throw await _streamError(e);
    }
    try {
      await for (final frame in parseSse(response.data!.stream)) {
        switch ((frame.event, jsonDecode(frame.data))) {
          case ('message', {'delta': final String delta}):
            yield delta;
          case ('done', _):
            return;
          case ('error', {'error': {'message': final String message}}):
            throw AiUnavailable(message);
        }
      }
    } on AppError {
      rethrow;
    } on FormatException {
      throw const BadResponse();
    } on DioException catch (e) {
      if (CancelToken.isCancel(e)) return;
      throw mapDioError(e);
    } on IOException {
      if (cancelToken?.isCancelled ?? false) return;
      throw const AiUnavailable.cutOff();
    }
    if (cancelToken?.isCancelled ?? false) return;
    throw const AiUnavailable.cutOff();
  }

  /// A failed streamed request carries its JSON error body as a byte stream.
  Future<AppError> _streamError(DioException e) async {
    final body = e.response?.data;
    if (body is! ResponseBody) return mapDioError(e);
    final Object? json;
    try {
      json = jsonDecode(await utf8.decodeStream(body.stream));
    } on Exception {
      return mapDioError(e);
    }
    return mapDioError(
      e.copyWith(
        response: Response<Object?>(
          requestOptions: e.requestOptions,
          statusCode: e.response?.statusCode,
          data: json,
        ),
      ),
    );
  }

  Future<Map<String, bool>> fetchConfig() => _send(
    () => _dio.get<Map<String, dynamic>>('/config'),
    (json) => (json['flags'] as Map<String, dynamic>).cast<String, bool>(),
  );

  /// [idempotencyKey] makes a replayed request safe: the server answers a
  /// repeated key with the original response instead of applying it twice.
  Future<Spot> patchSpot(
    String id,
    Map<String, dynamic> patch, {
    required String idempotencyKey,
  }) => _send(
    () => _dio.patch<Map<String, dynamic>>(
      '/spots/$id',
      data: patch,
      options: Options(headers: {'Idempotency-Key': idempotencyKey}),
    ),
    Spot.fromJson,
  );

  Future<R> _send<T, R>(
    Future<Response<T>> Function() request,
    R Function(T body) parse,
  ) async {
    final T? body;
    try {
      body = (await request()).data;
    } on DioException catch (e) {
      throw mapDioError(e);
    }
    if (body == null) throw const BadResponse();
    try {
      return parse(body);
    } on Object {
      throw const BadResponse();
    }
  }
}
