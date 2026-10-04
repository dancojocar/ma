import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:shared_preferences_platform_interface/in_memory_shared_preferences_async.dart';
import 'package:shared_preferences_platform_interface/shared_preferences_async_platform_interface.dart';
import 'package:unieats/app_router.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats/main.dart';
import 'package:unieats/providers/providers.dart';
import 'package:unieats/services/crash_reporter.dart';
import 'package:unieats/ui/spot_detail/describe_dish_card.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

final spots = [
  spot('spot-3', name: 'Pizza Stop', category: SpotCategory.fastfood),
  spot('spot-2', name: 'Espresso Lab'),
];
final reviews = [
  review('review-1', spotId: 'spot-3'),
  review('review-2', spotId: 'spot-3'),
];

/// The whole app (router, screens, controllers) over in-memory data, with
/// no WebSocket, plugin or network.
Future<FakeAuthRepository> pumpApp(
  WidgetTester tester, {
  Session session = const SignedOut(),
  ScriptedApi? api,
}) async {
  SharedPreferencesAsyncPlatform.instance =
      InMemorySharedPreferencesAsync.empty();
  final auth = FakeAuthRepository(session);
  await tester.pumpWidget(
    ProviderScope(
      retry: (_, _) => null,
      overrides: [
        authRepositoryProvider.overrideWithValue(auth),
        spotRepositoryProvider.overrideWithValue(InMemorySpotRepository(spots)),
        reviewRepositoryProvider.overrideWithValue(
          InMemoryReviewRepository(reviews),
        ),
        apiClientProvider.overrideWithValue(api ?? ScriptedApi()),
        outboxSyncProvider.overrideWith((_) {}),
        liveSyncProvider.overrideWith((_) {}),
        liveEventsProvider.overrideWith((_) => const Stream.empty()),
        crashReporterProvider.overrideWithValue(
          ConsentGatedCrashReporter(const LoggingCrashReporter()),
        ),
      ],
      child: const UniEatsApp(),
    ),
  );
  // Past the 350 ms card entrance and every route transition.
  await tester.pumpAndSettle();
  return auth;
}

String location(WidgetTester tester) =>
    ProviderScope.containerOf(tester.element(find.byType(UniEatsApp)))
        .read(routerProvider)
        .state
        .uri
        .toString();

Future<void> signOutInSettings(WidgetTester tester) async {
  await tester.tap(find.byTooltip('Settings'));
  await tester.pumpAndSettle();
  // Below the fold on the 800×600 test surface.
  await tester.scrollUntilVisible(find.text('Sign out'), 100);
  await tester.tap(find.text('Sign out'));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('the Maestro journey: sign in, first spot, reviews, describe, '
      'back, sign out in Settings', (tester) async {
    final semantics = tester.ensureSemantics();
    final api = ScriptedApi();
    final auth = await pumpApp(tester, api: api);

    expect(find.text('Sign In'), findsOneWidget);
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Email'),
      'student@unieats.app',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Password'),
      'password',
    );
    await tester.tap(find.text('Sign In'));
    await tester.pumpAndSettle();

    expect(auth.calls, ['login student@unieats.app']);
    expect(location(tester), '/spots');
    expect(find.bySemanticsIdentifier('spot-list-item'), findsNWidgets(2));

    await tester.tap(find.bySemanticsIdentifier('spot-list-item').first);
    await tester.pumpAndSettle();

    expect(location(tester), '/spots/spot-3');
    expect(find.bySemanticsIdentifier('spot-detail-reviews'), findsOneWidget);
    expect(find.text('Reviews (2)'), findsOneWidget);

    final button = find.bySemanticsIdentifier('describe-dish-button');
    await tester.ensureVisible(button);
    await tester.tap(button);
    await tester.pump();
    api.describeStreams.single.add('Pizza by the slice.');
    await api.describeStreams.single.close();
    await tester.pumpAndSettle();

    expect(find.bySemanticsIdentifier('describe-dish-result'), findsOneWidget);
    expect(find.text('Pizza by the slice.'), findsOneWidget);
    expect(find.textContaining('Server unreachable'), findsNothing);

    await tester.tap(find.byTooltip('Back'));
    await tester.pumpAndSettle();
    expect(find.bySemanticsIdentifier('spot-list-item'), findsNWidgets(2));

    await signOutInSettings(tester);

    expect(auth.calls.last, 'logout');
    expect(find.text('Sign In'), findsOneWidget);
    expect(location(tester), '/login?from=%2Fspots');
    semantics.dispose();
  });

  testWidgets('a deep link opened while signed out lands on its spot after '
      'sign-in', (tester) async {
    await pumpApp(tester);
    final router = ProviderScope.containerOf(
      tester.element(find.byType(UniEatsApp)),
    ).read(routerProvider);

    router.go('/spots/spot-3');
    await tester.pumpAndSettle();
    expect(location(tester), '/login?from=%2Fspots%2Fspot-3');

    await tester.enterText(find.widgetWithText(TextFormField, 'Email'), 'a@x');
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Password'),
      'password',
    );
    await tester.tap(find.text('Sign In'));
    await tester.pumpAndSettle();

    expect(location(tester), '/spots/spot-3');
    expect(find.text('Reviews (2)'), findsOneWidget);
  });

  group('unlock screen', () {
    testWidgets('a cancelled prompt stays on the unlock screen', (
      tester,
    ) async {
      final auth = FakeAuthRepository(const Locked(alice))
        ..unlockError = const BiometricCancelled();
      SharedPreferencesAsyncPlatform.instance =
          InMemorySharedPreferencesAsync.empty();
      await tester.pumpWidget(
        ProviderScope(
          retry: (_, _) => null,
          overrides: [
            authRepositoryProvider.overrideWithValue(auth),
            outboxSyncProvider.overrideWith((_) {}),
          ],
          child: const UniEatsApp(),
        ),
      );
      await tester.pumpAndSettle();

      expect(auth.calls, ['unlock'], reason: 'prompts on arrival');
      expect(find.text('Signed in as Alice'), findsOneWidget);
      expect(location(tester), '/unlock?from=%2Fspots');

      await tester.tap(find.text('Use password instead'));
      await tester.pumpAndSettle();

      expect(auth.calls.last, 'logout');
      expect(find.text('Sign In'), findsOneWidget);
    });

    testWidgets('a successful prompt opens the list', (tester) async {
      await pumpApp(tester, session: const Locked(alice));

      expect(location(tester), '/spots');
      expect(find.text('Pizza Stop'), findsOneWidget);
    });

    testWidgets('a changed biometric signs out with a notice', (tester) async {
      final auth = FakeAuthRepository(const Locked(alice))
        ..unlockError = const BiometricInvalidated();
      await tester.pumpWidget(
        ProviderScope(
          retry: (_, _) => null,
          overrides: [
            authRepositoryProvider.overrideWithValue(auth),
            outboxSyncProvider.overrideWith((_) {}),
          ],
          child: const UniEatsApp(),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Sign In'), findsOneWidget);
      expect(
        find.text(const BiometricInvalidated().toString()),
        findsOneWidget,
      );
    });
  });

  group('Settings', () {
    testWidgets('biometric switch is off by default and turns on', (
      tester,
    ) async {
      final auth = await pumpApp(tester, session: const SignedIn(alice));
      await tester.tap(find.byTooltip('Settings'));
      await tester.pumpAndSettle();

      final toggle = find.widgetWithText(
        SwitchListTile,
        'Unlock with biometrics',
      );
      await tester.scrollUntilVisible(toggle, 100);
      expect(tester.widget<SwitchListTile>(toggle).value, isFalse);

      await tester.tap(toggle);
      await tester.pumpAndSettle();

      expect(auth.biometricOn, isTrue);
      expect(tester.widget<SwitchListTile>(toggle).value, isTrue);
    });

    testWidgets('no biometric switch without a strong biometric', (
      tester,
    ) async {
      final auth = FakeAuthRepository(const SignedIn(alice))
        ..biometricsOnDevice = false;
      SharedPreferencesAsyncPlatform.instance =
          InMemorySharedPreferencesAsync.empty();
      await tester.pumpWidget(
        ProviderScope(
          retry: (_, _) => null,
          overrides: [
            authRepositoryProvider.overrideWithValue(auth),
            spotRepositoryProvider.overrideWithValue(
              InMemorySpotRepository(spots),
            ),
            outboxSyncProvider.overrideWith((_) {}),
            liveSyncProvider.overrideWith((_) {}),
            liveEventsProvider.overrideWith((_) => const Stream.empty()),
          ],
          child: const UniEatsApp(),
        ),
      );
      await tester.pumpAndSettle();
      await tester.tap(find.byTooltip('Settings'));
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(find.text('Sign out'), 100);

      expect(find.text('Unlock with biometrics'), findsNothing);
    });

    testWidgets('Test crash sends nothing until crash reports are on', (
      tester,
    ) async {
      await pumpApp(tester, session: const SignedIn(alice));
      await tester.tap(find.byTooltip('Settings'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Test crash'));
      await tester.pumpAndSettle();
      expect(find.text('Not sent: crash reporting is off'), findsOneWidget);
    });
  });

  group('DescribeDishCard', () {
    late ScriptedApi api;

    Future<void> pumpCard(WidgetTester tester) async {
      api = ScriptedApi();
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            apiClientProvider.overrideWithValue(api),
            signedInUserProvider.overrideWithValue(alice),
          ],
          child: MaterialApp(
            home: Scaffold(body: DescribeDishCard(spot: spots.first)),
          ),
        ),
      );
    }

    testWidgets('streams the text and announces it once, when complete', (
      tester,
    ) async {
      final semantics = tester.ensureSemantics();
      await pumpCard(tester);

      await tester.tap(find.text('Describe this dish'));
      await tester.pump();
      expect(find.bySemanticsLabel(RegExp('Describing')), findsOneWidget);
      await tester.tap(find.text('Describe this dish'));
      expect(api.describeStreams, hasLength(1), reason: 'disabled while busy');

      api.describeStreams.single.add('Pizza ');
      await tester.pump();
      expect(
        tester.getSemantics(find.text('Pizza ')).flagsCollection.isLiveRegion,
        isFalse,
      );

      api.describeStreams.single.add('by the slice.');
      await api.describeStreams.single.close();
      await tester.pump();
      expect(
        tester
            .getSemantics(find.text('Pizza by the slice.'))
            .flagsCollection
            .isLiveRegion,
        isTrue,
      );
      semantics.dispose();
    });

    testWidgets('shows a readable error', (tester) async {
      await pumpCard(tester);

      await tester.tap(find.text('Describe this dish'));
      await tester.pump();
      api.describeStreams.last.addError(const NoConnectivity());
      await tester.pump();
      expect(
        find.text('Server unreachable. Is the UniEats server running?'),
        findsOneWidget,
      );

      await tester.tap(find.text('Describe this dish'));
      await tester.pump();
      api.describeStreams.last.addError(const HttpError(429, 'At most 10'));
      await tester.pump();
      expect(
        find.text('Too many requests: try again in a minute.'),
        findsOneWidget,
      );
    });

    testWidgets('leaving the screen cancels the stream', (tester) async {
      await pumpCard(tester);
      await tester.tap(find.text('Describe this dish'));
      await tester.pump();
      expect(api.describeStreams.single.hasListener, isTrue);

      await tester.pumpWidget(const SizedBox());

      expect(api.describeStreams.single.hasListener, isFalse);
    });
  });
}
