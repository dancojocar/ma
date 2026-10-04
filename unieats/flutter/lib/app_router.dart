import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'ui/spot_detail/spot_detail_screen.dart';
import 'ui/spot_list/spot_list_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final router = GoRouter(
    initialLocation: '/spots',
    redirect: (context, state) => deepLinkToPath(state.uri),
    routes: [
      GoRoute(path: '/', redirect: (_, _) => '/spots'),
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
  ref.onDispose(router.dispose);
  return router;
});

/// The platform hands Flutter the whole URL. `unieats://spots/spot-3` parses with
/// host `spots` and path `/spot-3`, so the host is folded back into the path;
/// `https://unieats.app/spots/spot-3` only needs its path.
String? deepLinkToPath(Uri uri) => switch (uri.scheme) {
  'unieats' => '/${uri.host}${uri.path}',
  'http' || 'https' => uri.path,
  _ => null,
};
