import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:shared_preferences_platform_interface/in_memory_shared_preferences_async.dart';
import 'package:shared_preferences_platform_interface/shared_preferences_async_platform_interface.dart';
import 'package:unieats/data/network/api_client.dart';
import 'package:unieats/providers/providers.dart';
import 'package:unieats/services/crash_reporter.dart';
import 'package:unieats/ui/common/spot_badges.dart';

import 'support/fakes.dart';

class RecordingReporter implements CrashReporter {
  final errors = <({Object error, bool fatal})>[];

  @override
  Future<void> recordError(
    Object error,
    StackTrace stack, {
    bool fatal = false,
  }) async => errors.add((error: error, fatal: fatal));
}

/// An [ApiClient] whose every request is answered with [body].
ApiClient configApi(Map<String, Object?> body, {List<String>? requests}) {
  final dio = Dio(BaseOptions(baseUrl: 'http://test/api'));
  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) {
        requests?.add('${options.method} ${options.path}');
        handler.resolve(
          Response(requestOptions: options, statusCode: 200, data: body),
        );
      },
    ),
  );
  return ApiClient(dio);
}

void main() {
  setUp(() {
    SharedPreferencesAsyncPlatform.instance =
        InMemorySharedPreferencesAsync.empty();
  });

  group('remote config', () {
    test('defaults until fetched; Fetch & activate applies GET /config at '
        'once', () async {
      final requests = <String>[];
      final c = ProviderContainer.test(
        overrides: [
          apiClientProvider.overrideWithValue(
            configApi({
              'flags': {'show_new_rating_ui': true},
            }, requests: requests),
          ),
        ],
      );
      expect(c.read(remoteConfigProvider), const RemoteFlags());

      await c.read(remoteConfigProvider.notifier).fetchAndActivate();

      expect(c.read(remoteConfigProvider).showNewRatingUi, isTrue);
      expect(c.read(remoteConfigProvider).fetchedAt, isNotNull);
      expect(requests, ['GET /config']);
    });

    test('a flag the server does not send falls back to false', () async {
      final c = ProviderContainer.test(
        overrides: [
          apiClientProvider.overrideWithValue(
            configApi({'flags': <String, Object?>{}}),
          ),
        ],
      );

      await c.read(remoteConfigProvider.notifier).fetchAndActivate();

      expect(c.read(remoteConfigProvider).showNewRatingUi, isFalse);
      expect(c.read(remoteConfigProvider).fetchedAt, isNotNull);
    });

    testWidgets('the rating turns into the new badge after a fetch', (
      tester,
    ) async {
      final api = ScriptedApi()..configFlags = {'show_new_rating_ui': true};
      final c = ProviderContainer.test(
        overrides: [apiClientProvider.overrideWithValue(api)],
      );
      final pizza = spot('s3', name: 'Pizza Stop');
      await tester.pumpWidget(
        UncontrolledProviderScope(
          container: c,
          child: MaterialApp(
            home: Scaffold(body: SpotMetaRow(spot: pizza)),
          ),
        ),
      );
      expect(find.byType(NewRatingBadge), findsNothing);

      await c.read(remoteConfigProvider.notifier).fetchAndActivate();
      await tester.pump();

      expect(find.byType(NewRatingBadge), findsOneWidget);
      expect(find.text('★ 4.0 / 5'), findsOneWidget);
    });
  });

  group('crash reporting consent', () {
    test('nothing is forwarded until the user opts in; the choice '
        'persists', () async {
      final delegate = RecordingReporter();
      final reporter = ConsentGatedCrashReporter(delegate);
      expect(await reporter.hasConsent(), isFalse);

      await reporter.recordError(StateError('before'), StackTrace.current);
      expect(delegate.errors, isEmpty);

      await reporter.setConsent(true);
      await reporter.recordError(
        StateError('after'),
        StackTrace.current,
        fatal: true,
      );
      expect(delegate.errors.single.fatal, isTrue);

      final restarted = RecordingReporter();
      final again = ConsentGatedCrashReporter(restarted);
      expect(await again.hasConsent(), isTrue);
      await again.setConsent(false);
      await again.recordError(StateError('off'), StackTrace.current);
      expect(restarted.errors, isEmpty);
    });

    test('the Settings switch writes through the app\'s reporter', () async {
      final delegate = RecordingReporter();
      final reporter = ConsentGatedCrashReporter(delegate);
      final c = ProviderContainer.test(
        retry: (_, _) => null,
        overrides: [crashReporterProvider.overrideWithValue(reporter)],
      );
      expect(await c.read(crashConsentProvider.future), isFalse);

      await c.read(crashConsentProvider.notifier).set(true);

      expect(c.read(crashConsentProvider).value, isTrue);
      await reporter.recordError(StateError('x'), StackTrace.current);
      expect(delegate.errors, hasLength(1));
    });
  });
}
