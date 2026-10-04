import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/app_router.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

void main() {
  String? redirect(AsyncValue<Session> auth, String location) =>
      authRedirect(auth, Uri.parse(location));

  const loading = AsyncLoading<Session>();
  const signedOut = AsyncData<Session>(SignedOut());
  const locked = AsyncData<Session>(Locked(alice));
  const signedIn = AsyncData<Session>(SignedIn(alice));

  group('authRedirect', () {
    test('while the session loads: splash, keeping the destination', () {
      expect(redirect(loading, '/spots'), '/splash?from=%2Fspots');
      expect(
        redirect(loading, '/spots/spot-3'),
        '/splash?from=%2Fspots%2Fspot-3',
      );
      expect(redirect(loading, '/splash?from=%2Fspots'), isNull);
    });

    test('signed out or failed: login, keeping ?from= across gates', () {
      expect(
        redirect(signedOut, '/spots/spot-3'),
        '/login?from=%2Fspots%2Fspot-3',
      );
      expect(
        redirect(signedOut, '/splash?from=%2Fspots%2Fspot-3'),
        '/login?from=%2Fspots%2Fspot-3',
      );
      expect(
        redirect(signedOut, '/unlock?from=%2Fspots%2Fspot-3'),
        '/login?from=%2Fspots%2Fspot-3',
      );
      expect(redirect(signedOut, '/unlock'), '/login');
      expect(redirect(signedOut, '/login?from=%2Fspots'), isNull);
      expect(
        redirect(AsyncError<Session>('boom', StackTrace.empty), '/spots'),
        '/login?from=%2Fspots',
      );
    });

    test('locked: unlock, keeping ?from=', () {
      expect(
        redirect(locked, '/splash?from=%2Fspots%2Fspot-3'),
        '/unlock?from=%2Fspots%2Fspot-3',
      );
      expect(
        redirect(locked, '/spots/spot-3'),
        '/unlock?from=%2Fspots%2Fspot-3',
      );
      expect(redirect(locked, '/unlock?from=%2Fspots%2Fspot-3'), isNull);
    });

    test('signed in: a gate goes to ?from= or the list, other routes stay', () {
      expect(
        redirect(signedIn, '/login?from=%2Fspots%2Fspot-3'),
        '/spots/spot-3',
      );
      expect(
        redirect(signedIn, '/unlock?from=%2Fspots%2Fspot-3'),
        '/spots/spot-3',
      );
      expect(redirect(signedIn, '/splash'), '/spots');
      expect(redirect(signedIn, '/spots/spot-3'), isNull);
      expect(redirect(signedIn, '/settings'), isNull);
    });
  });

  test('deepLinkToPath folds the custom scheme host into the path', () {
    expect(
      deepLinkToPath(Uri.parse('unieats://spots/spot-3')),
      '/spots/spot-3',
    );
    expect(
      deepLinkToPath(Uri.parse('https://unieats.app/spots/spot-3')),
      '/spots/spot-3',
    );
    expect(deepLinkToPath(Uri.parse('/spots/spot-3')), isNull);
  });
}
