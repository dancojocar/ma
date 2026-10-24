import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../providers/providers.dart';
import '../common/spot_badges.dart';
import '../common/spot_photo.dart';

class SpotDetailScreen extends ConsumerWidget {
  const SpotDetailScreen({super.key, required this.spotId});

  final String spotId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final spot = ref.watch(spotByIdProvider(spotId));
    if (spot == null) {
      return Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Spot "$spotId" not found')),
      );
    }
    final isFavourite = ref.watch(
      spotListProvider.select((s) => s.favouriteIds.contains(spotId)),
    );

    return Scaffold(
      appBar: AppBar(
        title: Text(spot.name),
        actions: [
          FavouriteButton(
            isFavourite: isFavourite,
            onToggle: () =>
                ref.read(spotListProvider.notifier).toggleFavourite(spotId),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SpotPhoto(
              url: spot.photoUrl,
              height: 220,
              semanticLabel: 'Photo of ${spot.name}',
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Text(
                  spot.name,
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              OpenBadge(openNow: spot.openNow),
            ],
          ),
          const SizedBox(height: 8),
          SpotMetaRow(spot: spot),
          const SizedBox(height: 12),
          Text(spot.description, style: theme.textTheme.bodyMedium),
        ],
      ),
    );
  }
}
