import 'package:flutter/material.dart';

import '../../data/seed_data.dart';
import '../../domain/models.dart';
import '../common/spot_badges.dart';
import '../spot_detail/spot_detail_screen.dart';

class SpotListScreen extends StatelessWidget {
  const SpotListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const spots = kSeedSpots;
    return Scaffold(
      appBar: AppBar(title: const Text('UniEats')),
      body: ListView.builder(
        padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
        itemCount: spots.length,
        itemBuilder: (context, index) {
          final spot = spots[index];
          return SpotCard(
            key: ValueKey(spot.id),
            spot: spot,
            onTap: () => Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => SpotDetailScreen(spot: spot),
              ),
            ),
          );
        },
      ),
    );
  }
}

class SpotCard extends StatelessWidget {
  const SpotCard({super.key, required this.spot, required this.onTap});

  final Spot spot;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      margin: const EdgeInsets.only(top: 12),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      spot.name,
                      style: theme.textTheme.titleMedium
                          ?.copyWith(fontWeight: FontWeight.bold),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  const SizedBox(width: 8),
                  OpenBadge(openNow: spot.openNow),
                ],
              ),
              const SizedBox(height: 6),
              SpotMetaRow(spot: spot),
              const SizedBox(height: 6),
              Text(
                spot.description,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.bodySmall
                    ?.copyWith(color: theme.colorScheme.onSurfaceVariant),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
