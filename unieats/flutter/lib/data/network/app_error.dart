import 'package:dio/dio.dart';

import '../../domain/models.dart';

sealed class AppError implements Exception {
  const AppError();

  String get message;

  @override
  String toString() => message;
}

class NoConnectivity extends AppError {
  const NoConnectivity();

  @override
  String get message => "Can't reach the UniEats server. Is it running?";
}

class TimeoutError extends AppError {
  const TimeoutError();

  @override
  String get message => 'The server took too long to answer.';
}

class HttpError extends AppError {
  const HttpError(this.statusCode, [this.serverMessage]);

  final int statusCode;
  final String? serverMessage;

  @override
  String get message => serverMessage ?? 'Server error ($statusCode).';
}

/// 409 from `PATCH /spots/:id`: the server holds a newer version.
class Conflict extends AppError {
  const Conflict(this.serverSpot);

  final Spot serverSpot;

  @override
  String get message => 'Someone else changed "${serverSpot.name}" first.';
}

class BadResponse extends AppError {
  const BadResponse();

  @override
  String get message => 'The server sent a response the app cannot read.';
}

AppError mapDioError(DioException e) => switch (e.type) {
  DioExceptionType.connectionTimeout ||
  DioExceptionType.sendTimeout ||
  DioExceptionType.receiveTimeout => const TimeoutError(),
  DioExceptionType.connectionError => const NoConnectivity(),
  DioExceptionType.badResponse when _conflictSpot(e.response) != null =>
    Conflict(_conflictSpot(e.response)!),
  DioExceptionType.badResponse => HttpError(
    e.response?.statusCode ?? -1,
    _serverMessage(e.response?.data),
  ),
  DioExceptionType.unknown when e.error is FormatException =>
    const BadResponse(),
  _ => const NoConnectivity(),
};

String? _serverMessage(Object? body) {
  if (body case {'error': {'message': final String message}}) return message;
  return null;
}

Spot? _conflictSpot(Response<Object?>? response) {
  if (response?.statusCode != 409) return null;
  if (response!.data case {'spot': final Map<String, dynamic> spot}) {
    return Spot.fromJson(spot);
  }
  return null;
}
