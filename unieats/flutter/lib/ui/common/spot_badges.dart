import 'package:flutter/material.dart';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:unieats_data/unieats_data.dart';
import '../../providers/providers.dart';

class OpenBadge extends StatelessWidget {
  const OpenBadge({super.key, required this.openNow});

  final bool openNow;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color:
            openNow
                ? Colors.green.withValues(alpha: 0.15)
                : Colors.red.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        openNow ? 'Open' : 'Closed',
        style: theme.textTheme.labelSmall?.copyWith(
          color: openNow ? Colors.green[700] : Colors.red[700],
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

class SpotMetaRow extends ConsumerWidget {
  const SpotMetaRow({super.key, required this.spot});

  final Spot spot;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final newRatingUi = ref.watch(
      remoteConfigProvider.select((f) => f.showNewRatingUi),
    );
    return Wrap(
      spacing: 12,
      runSpacing: 4,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        if (newRatingUi)
          NewRatingBadge(rating: spot.rating)
        else
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.star_rounded, size: 16, color: Colors.amber[700]),
              const SizedBox(width: 4),
              Text(
                spot.rating.toStringAsFixed(1),
                style: theme.textTheme.bodySmall,
              ),
            ],
          ),
        Text(
          r'$' * spot.priceLevel,
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        Chip(
          label: Text(spot.category.label),
          labelStyle: theme.textTheme.labelSmall,
          padding: EdgeInsets.zero,
          visualDensity: VisualDensity.compact,
        ),
      ],
    );
  }
}

class FavouriteButton extends StatelessWidget {
  const FavouriteButton({
    super.key,
    required this.isFavourite,
    required this.onToggle,
  });

  final bool isFavourite;
  final VoidCallback onToggle;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      tooltip: isFavourite ? 'Remove from favourites' : 'Add to favourites',
      icon: Icon(isFavourite ? Icons.favorite : Icons.favorite_border),
      color: isFavourite ? Colors.red : null,
      onPressed: onToggle,
    );
  }
}

/// The redesigned rating shown when the `show_new_rating_ui` flag is on.
class NewRatingBadge extends StatelessWidget {
  const NewRatingBadge({super.key, required this.rating});

  final double rating;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        gradient: LinearGradient(colors: [scheme.primary, scheme.tertiary]),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        '★ ${rating.toStringAsFixed(1)} / 5',
        style: TextStyle(
          color: scheme.onPrimary,
          fontWeight: FontWeight.bold,
          fontSize: 12,
        ),
      ),
    );
  }
}
