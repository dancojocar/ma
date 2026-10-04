import 'dart:convert';

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
    CancelToken? cancelToken,
  }) => _send(
    () => _dio.get<Map<String, dynamic>>(
      '/spots',
      queryParameters: {
        'page': page,
        'limit': limit,
        if (query != null && query.isNotEmpty) 'q': query,
        if (category != null) 'category': category,
      },
      cancelToken: cancelToken,
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
  Stream<String> describeSpot(Spot spot) async* {
    final Response<ResponseBody> response;
    try {
      response = await _dio.post<ResponseBody>(
        '/ai/describe',
        data: {'spot': spot.toJson()},
        options: Options(
          responseType: ResponseType.stream,
          receiveTimeout: const Duration(seconds: 60),
          headers: {'Accept': 'text/event-stream'},
        ),
      );
    } on DioException catch (e) {
      throw mapDioError(e);
    }
    try {
      await for (final frame in parseSse(response.data!.stream)) {
        final json = jsonDecode(frame.data);
        switch ((frame.event, json)) {
          case ('message', {'delta': final String delta}):
            yield delta;
          case ('done', _):
            return;
          case ('error', {'error': {'message': final String message}}):
            throw AiUnavailable(message);
        }
      }
    } on FormatException {
      throw const BadResponse();
    } on DioException catch (e) {
      throw mapDioError(e);
    }
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
