import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../domain/models.dart';
import '../../providers/providers.dart';
import '../common/spot_badges.dart';
import 'spot_list_widgets.dart';

class SpotListScreen extends ConsumerWidget {
  const SpotListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(spotListProvider);
    final notifier = ref.read(spotListProvider.notifier);
    final spots = state.visibleSpots;

    return Scaffold(
      appBar: AppBar(title: const Text('UniEats')),
      body: Column(
        children: [
          SpotSearchBar(
            query: state.searchQuery,
            onQueryChange: notifier.setSearchQuery,
          ),
          CategoryFilterRow(
            selected: state.categoryFilter,
            onSelected: notifier.setCategoryFilter,
          ),
          Expanded(
            child:
                spots.isEmpty
                    ? const EmptySpotsMessage()
                    : ListView.builder(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                      itemCount: spots.length,
                      itemBuilder: (context, index) {
                        final spot = spots[index];
                        return SpotCard(
                          key: ValueKey(spot.id),
                          spot: spot,
                          isFavourite: state.favouriteIds.contains(spot.id),
                          onFavouriteToggle:
                              () => notifier.toggleFavourite(spot.id),
                          onTap: () => context.push('/spots/${spot.id}'),
                        );
                      },
                    ),
          ),
        ],
      ),
    );
  }
}

class SpotCard extends StatelessWidget {
  const SpotCard({
    super.key,
    required this.spot,
    required this.isFavourite,
    required this.onFavouriteToggle,
    required this.onTap,
  });

  final Spot spot;
  final bool isFavourite;
  final VoidCallback onFavouriteToggle;
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
          padding: const EdgeInsets.fromLTRB(12, 4, 4, 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      spot.name,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  OpenBadge(openNow: spot.openNow),
                  FavouriteButton(
                    isFavourite: isFavourite,
                    onToggle: onFavouriteToggle,
                  ),
                ],
              ),
              SpotMetaRow(spot: spot),
              const SizedBox(height: 6),
              Padding(
                padding: const EdgeInsets.only(right: 8),
                child: Text(
                  spot.description,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
