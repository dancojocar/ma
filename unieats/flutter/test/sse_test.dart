import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/auth/auth_interceptor.dart';
import 'package:unieats/data/auth/token_store.dart';
import 'package:unieats/data/network/api_client.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats/data/network/sse.dart';

import 'support/fakes.dart';

Stream<List<int>> chunks(List<String> parts) =>
    Stream.fromIterable(parts.map(utf8.encode));

/// A local HTTP server for `POST /api/ai/describe`; [respond] writes the
/// answer to each request.
class DescribeServer {
  late HttpServer _server;
  Future<void> Function(HttpResponse response) respond = (r) => r.close();
  final requests = <({Map<String, dynamic> body, String? authorization})>[];

  String get baseUrl => 'http://127.0.0.1:${_server.port}/api';

  Future<void> start() async {
    _server = await HttpServer.bind(InternetAddress.loopbackIPv4, 0);
    _server.listen((request) async {
      requests.add((
        body: jsonDecode(
          await utf8.decodeStream(request),
        ) as Map<String, dynamic>,
        authorization: request.headers.value(HttpHeaders.authorizationHeader),
      ));
      await respond(request.response);
    });
  }

  Future<void> stop() => _server.close(force: true);
}

Future<void> Function(HttpResponse) sseFrames(List<String> frames) =>
    (response) async {
      response
        ..statusCode = 200
        ..bufferOutput = false
        ..headers.contentType = ContentType('text', 'event-stream');
      for (final frame in frames) {
        response.write(frame);
        await response.flush();
      }
      await response.close();
    };

Future<void> Function(HttpResponse) jsonError(int status, String message) =>
    (response) async {
      response
        ..statusCode = status
        ..headers.contentType = ContentType.json
        ..write(
          jsonEncode({
            'error': {'code': 'x', 'message': message},
          }),
        );
      await response.close();
    };

void main() {
  group('parseSse', () {
    test('splits deltas and the done event', () async {
      const body =
          'data: {"delta":"Pizza Stop is "}\n\n'
          'data: {"delta":"open."}\n\n'
          'event: done\ndata: {"source":"template"}\n\n';

      final frames = await parseSse(Stream.value(utf8.encode(body))).toList();

      expect(frames.map((f) => f.event), ['message', 'message', 'done']);
      expect(jsonDecode(frames.first.data), {'delta': 'Pizza Stop is '});
    });

    test('handles comments, CRLF, frames split across chunks and multi-line '
        'data; drops a frame cut off by the end', () async {
      final frames = await parseSse(
        chunks([
          ': keep-alive\r\n',
          'data: {"del',
          'ta":"a"}\r',
          '\n\r\n',
          'event: done\ndata: x\ndata:  y\n\n',
          'id: 7\nretry: 10\n\n',
          'data:nospace\n\n',
          'event: error\ndata: cut',
        ]),
      ).toList();

      expect(frames, [
        (event: 'message', data: '{"delta":"a"}'),
        (event: 'done', data: 'x\n y'),
        (event: 'message', data: 'nospace'),
      ]);
    });

    test('decodes UTF-8 split across chunks', () async {
      final bytes = utf8.encode('data: café ☕\n\n');

      final frames = await parseSse(
        Stream.fromIterable([
          bytes.sublist(0, 9),
          bytes.sublist(9, 13),
          bytes.sublist(13),
        ]),
      ).toList();

      expect(frames.single.data, 'café ☕');
    });
  });

  group('ApiClient.describeSpot', () {
    late DescribeServer server;
    late ApiClient api;
    late int unauthorized;
    final pizza = spot('spot-3', name: 'Pizza Stop');

    setUp(() async {
      FlutterSecureStorage.setMockInitialValues({'jwt': 'token-123'});
      unauthorized = 0;
      server = DescribeServer();
      await server.start();
      final dio = createDio(server.baseUrl)
        ..interceptors.insert(
          0,
          AuthInterceptor(TokenStore(), onUnauthorized: () => unauthorized++),
        );
      api = ApiClient(dio);
    });
    tearDown(() => server.stop());

    test('streams each delta until done; posts the full spot with the '
        'token', () async {
      server.respond = sseFrames([
        'data: {"delta":"Hello "}\n\n',
        'data: {"delta":"world"}\n\n',
        'event: done\ndata: {"source":"template"}\n\n',
      ]);

      expect(await api.describeSpot(pizza).toList(), ['Hello ', 'world']);
      expect(server.requests.single.authorization, 'Bearer token-123');
      expect(server.requests.single.body['spot'], pizza.toJson());
    });

    test('event: error ends the stream with the server message', () async {
      server.respond = sseFrames([
        'data: {"delta":"Hi"}\n\n',
        'event: error\ndata: {"error":{"code":"refused","message":"Nope"}}\n\n',
      ]);
      final deltas = <String>[];

      await expectLater(
        api.describeSpot(pizza).map(deltas.add).drain<void>(),
        throwsA(
          isA<AiUnavailable>().having((e) => e.message, 'message', 'Nope'),
        ),
      );
      expect(deltas, ['Hi']);
    });

    test('a stream that stops before done is cut off', () async {
      server.respond = sseFrames(['data: {"delta":"Hi"}\n\n']);

      await expectLater(
        api.describeSpot(pizza).toList(),
        throwsA(isA<AiUnavailable>()),
      );
    });

    test('a frame that is not JSON is a bad response', () async {
      server.respond = sseFrames(['data: {oops\n\n']);

      await expectLater(
        api.describeSpot(pizza).toList(),
        throwsA(isA<BadResponse>()),
      );
    });

    test('429 carries the server message', () async {
      server.respond = jsonError(429, 'Slow down');

      await expectLater(
        api.describeSpot(pizza).toList(),
        throwsA(
          isA<HttpError>()
              .having((e) => e.statusCode, 'statusCode', 429)
              .having((e) => e.message, 'message', 'Slow down'),
        ),
      );
    });

    test('401 ends the session like any other request', () async {
      server.respond = jsonError(401, 'expired');

      await expectLater(
        api.describeSpot(pizza).toList(),
        throwsA(isA<HttpError>()),
      );
      expect(unauthorized, 1);
    });

    test('a server that is down is NoConnectivity', () async {
      final down = ApiClient(createDio(server.baseUrl));
      await server.stop();

      await expectLater(
        down.describeSpot(pizza).toList(),
        throwsA(isA<NoConnectivity>()),
      );
    });
  });
}
