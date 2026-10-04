import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'providers/providers.dart';
import 'ui/login/login_screen.dart';
import 'ui/spot_detail/spot_detail_screen.dart';
import 'ui/spot_list/spot_list_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final authChanges = ValueNotifier<AsyncValue<Object?>>(
    ref.read(authProvider),
  );
  ref.listen(authProvider, (_, next) => authChanges.value = next);

  final router = GoRouter(
    initialLocation: '/spots',
    refreshListenable: authChanges,
    redirect:
        (context, state) =>
            deepLinkToPath(state.uri) ??
            _authRedirect(ref.read(authProvider), state),
    routes: [
      GoRoute(path: '/', redirect: (_, _) => '/spots'),
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(
        path: '/spots',
        builder: (context, state) => const SpotListScreen(),
        routes: [
          GoRoute(
            path: ':id',
            builder:
                (context, state) =>
                    SpotDetailScreen(spotId: state.pathParameters['id']!),
          ),
        ],
      ),
    ],
    errorBuilder:
        (context, state) => Scaffold(
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

/// Without a session everything goes to `/login?from=<where you were going>`,
/// so a deep link still lands on its spot after signing in.
String? _authRedirect(AsyncValue<Object?> auth, GoRouterState state) {
  if (auth.isLoading && !auth.hasValue && !auth.hasError) return null;
  final signedIn = auth.valueOrNull != null;
  final atLogin = state.matchedLocation == '/login';
  if (!signedIn && !atLogin) {
    return Uri(
      path: '/login',
      queryParameters: {'from': state.uri.toString()},
    ).toString();
  }
  if (signedIn && atLogin) return state.uri.queryParameters['from'] ?? '/spots';
  return null;
}

/// The platform hands Flutter the whole URL. `unieats://spots/spot-3` parses with
/// host `spots` and path `/spot-3`, so the host is folded back into the path;
/// `https://unieats.app/spots/spot-3` only needs its path.
String? deepLinkToPath(Uri uri) => switch (uri.scheme) {
  'unieats' => '/${uri.host}${uri.path}',
  'http' || 'https' => uri.path,
  _ => null,
};
