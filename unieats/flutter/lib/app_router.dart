import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import 'domain/models.dart';
import 'providers/providers.dart';
import 'ui/login/login_screen.dart';
import 'ui/login/unlock_screen.dart';
import 'ui/nearby/nearby_spots_screen.dart';
import 'ui/spot_detail/spot_detail_screen.dart';
import 'ui/spot_list/spot_list_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final authChanges = ValueNotifier<AsyncValue<Session>>(
    ref.read(authProvider),
  );
  ref.listen(authProvider, (_, next) => authChanges.value = next);

  final router = GoRouter(
    initialLocation: '/spots',
    refreshListenable: authChanges,
    redirect: (context, state) =>
        deepLinkToPath(state.uri) ??
        authRedirect(ref.read(authProvider), state.uri),
    routes: [
      GoRoute(path: '/', redirect: (_, _) => '/spots'),
      GoRoute(
        path: '/splash',
        builder: (_, _) =>
            const Scaffold(body: Center(child: CircularProgressIndicator())),
      ),
      GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
      GoRoute(path: '/unlock', builder: (_, _) => const UnlockScreen()),
      GoRoute(
        path: '/spots',
        builder: (context, state) => const SpotListScreen(),
        routes: [
          GoRoute(
            path: ':id',
            builder: (context, state) =>
                SpotDetailScreen(spotId: state.pathParameters['id']!),
          ),
        ],
      ),
      GoRoute(
        path: '/nearby',
        builder: (context, state) => const NearbySpotsScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(),
      body: Center(child: Text('Page not found: ${state.uri}')),
    ),
  );
  ref.onDispose(() {
    router.dispose();
    authChanges.dispose();
  });
  return router;
});

const _gates = {'/splash', '/login', '/unlock'};

/// Sends [uri] to the screen the session allows, or returns null to stay.
/// The splash, unlock and login screens carry the original destination in
/// `?from=`, so a deep link still lands on its spot afterwards.
String? authRedirect(AsyncValue<Session> auth, Uri uri) {
  final atGate = _gates.contains(uri.path);
  final from = atGate ? uri.queryParameters['from'] : uri.toString();
  String? gate(String path) => uri.path == path
      ? null
      : Uri(
          path: path,
          queryParameters: from == null ? null : {'from': from},
        ).toString();

  return switch (auth) {
    AsyncValue(value: SignedIn()) => atGate ? from ?? '/spots' : null,
    AsyncValue(value: Locked()) => gate('/unlock'),
    AsyncValue(value: SignedOut()) ||
    AsyncValue(hasError: true) => gate('/login'),
    _ => gate('/splash'),
  };
}

/// The platform hands Flutter the whole URL. `unieats://spots/spot-3` parses with
/// host `spots` and path `/spot-3`, so the host is folded back into the path;
/// `https://unieats.app/spots/spot-3` only needs its path.
String? deepLinkToPath(Uri uri) => switch (uri.scheme) {
  'unieats' => '/${uri.host}${uri.path}',
  'http' || 'https' => uri.path,
  _ => null,
};
