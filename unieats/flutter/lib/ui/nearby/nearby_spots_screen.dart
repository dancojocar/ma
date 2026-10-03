import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/providers.dart';
import '../../services/location_service.dart';
import '../common/spot_badges.dart';
import '../common/status_views.dart';

class NearbySpotsScreen extends ConsumerWidget {
  const NearbySpotsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen(appLifecycleProvider, (previous, next) {
      if (next == AppLifecycleState.resumed) {
        ref.invalidate(locationAccessProvider);
      }
    });
    final access = ref.watch(locationAccessProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Spots near me')),
      body: switch (access) {
        AsyncValue(valueOrNull: LocationAccess.granted) => const _NearbyList(),
        AsyncValue(valueOrNull: final LocationAccess denied) => _AccessNeeded(
          access: denied,
          onAllow: () async {
            final location = ref.read(locationServiceProvider);
            if (denied == LocationAccess.deniedForever) {
              await location.openSettings();
            } else {
              await location.checkAccess(request: true);
            }
            ref.invalidate(locationAccessProvider);
          },
        ),
        AsyncValue(:final error?) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(locationAccessProvider),
        ),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }
}

class _AccessNeeded extends StatelessWidget {
  const _AccessNeeded({required this.access, required this.onAllow});

  final LocationAccess access;
  final VoidCallback onAllow;

  @override
  Widget build(BuildContext context) {
    final (message, action) = switch (access) {
      LocationAccess.serviceDisabled => (
        'Location services are off. Turn them on to see spots near you.',
        'Try again',
      ),
      LocationAccess.deniedForever => (
        'Location access is blocked for UniEats. Allow it in Settings to see '
            'spots near you.',
        'Open settings',
      ),
      _ => (
        'UniEats uses your location only while this screen is open, to list '
            'spots within 2 km of you.',
        'Allow location',
      ),
    };
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.location_on_outlined, size: 56),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton(onPressed: onAllow, child: Text(action)),
          ],
        ),
      ),
    );
  }
}

class _NearbyList extends ConsumerWidget {
  const _NearbyList();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final position = ref.watch(positionProvider);
    final nearby = ref.watch(nearbySpotsProvider);
    if (position case AsyncValue(:final error?)) {
      return ErrorView(
        error: error,
        onRetry: () => ref.invalidate(positionProvider),
      );
    }
    return switch (nearby) {
      AsyncValue(valueOrNull: final spots?) when spots.isEmpty => const Center(
        child: Text('No spots within 2 km of you.'),
      ),
      AsyncValue(valueOrNull: final spots?) => ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: spots.length,
        itemBuilder: (context, index) {
          final (:spot, :meters) = spots[index];
          return Card(
            child: ListTile(
              leading: const Icon(Icons.restaurant),
              title: Text(spot.name),
              subtitle: Text('${spot.category.label} · ${_distance(meters)}'),
              trailing: OpenBadge(openNow: spot.openNow),
              onTap: () => context.push('/spots/${spot.id}'),
            ),
          );
        },
      ),
      _ => const Center(child: CircularProgressIndicator()),
    };
  }

  String _distance(double meters) =>
      meters < 1000
          ? '${meters.round()} m'
          : '${(meters / 1000).toStringAsFixed(1)} km';
}
