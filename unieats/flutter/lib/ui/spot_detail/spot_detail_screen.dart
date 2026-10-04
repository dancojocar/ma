import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models.dart';
import '../../providers/providers.dart';
import '../common/spot_badges.dart';
import '../common/status_views.dart';

class SpotDetailScreen extends ConsumerWidget {
  const SpotDetailScreen({super.key, required this.spotId});

  final String spotId;

  Future<void> _refresh(WidgetRef ref) async {
    ref.invalidate(reviewsProvider(spotId));
    await ref
        .refresh(spotDetailProvider(spotId).future)
        // A failed refresh is rendered from the provider's error state.
        .then<void>((_) {}, onError: (_) {});
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final spot = ref.watch(spotDetailProvider(spotId));
    final isFavourite = ref.watch(
      spotListProvider.select((s) => s.favouriteIds.contains(spotId)),
    );

    return Scaffold(
      appBar: AppBar(
        title: Text(spot.valueOrNull?.name ?? ''),
        actions: [
          FavouriteButton(
            isFavourite: isFavourite,
            onToggle:
                () =>
                    ref.read(spotListProvider.notifier).toggleFavourite(spotId),
          ),
        ],
      ),
      body: switch (spot) {
        AsyncValue(:final error?) when !spot.isLoading => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(spotDetailProvider(spotId)),
        ),
        AsyncValue(valueOrNull: final value?) => RefreshIndicator(
          onRefresh: () => _refresh(ref),
          child: _SpotDetailBody(spot: value),
        ),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }
}

class _SpotDetailBody extends ConsumerWidget {
  const _SpotDetailBody({required this.spot});

  final Spot spot;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final reviews = ref.watch(reviewsProvider(spot.id));

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        SpotPhoto(url: spot.photoUrl, height: 220),
        Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
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
              const SizedBox(height: 24),
              Text(
                reviews.hasValue
                    ? 'Reviews (${reviews.requireValue.length})'
                    : 'Reviews',
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              switch (reviews) {
                AsyncValue(:final error?) when !reviews.isLoading => Row(
                  children: [
                    Expanded(child: Text('Reviews unavailable: $error')),
                    TextButton(
                      onPressed: () => ref.invalidate(reviewsProvider(spot.id)),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
                AsyncValue(valueOrNull: final value?) when value.isEmpty =>
                  const Text('No reviews yet.'),
                AsyncValue(valueOrNull: final value?) => Column(
                  children: [for (final r in value) ReviewTile(review: r)],
                ),
                _ => const Center(child: CircularProgressIndicator()),
              },
            ],
          ),
        ),
      ],
    );
  }
}

class ReviewTile extends StatelessWidget {
  const ReviewTile({super.key, required this.review});

  final Review review;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final date = DateTime.fromMillisecondsSinceEpoch(review.createdAt);
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Row(
          children: [
            Expanded(child: Text(review.author)),
            for (var i = 1; i <= 5; i++)
              Icon(
                i <= review.stars
                    ? Icons.star_rounded
                    : Icons.star_outline_rounded,
                size: 16,
                color: Colors.amber[700],
              ),
          ],
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (review.text.isNotEmpty) Text(review.text),
            Text(
              '${date.day}/${date.month}/${date.year}',
              style: theme.textTheme.labelSmall,
            ),
          ],
        ),
      ),
    );
  }
}
