import 'package:flutter/material.dart';

import '../../domain/models.dart';

class OpenBadge extends StatelessWidget {
  const OpenBadge({super.key, required this.openNow});

  final bool openNow;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: openNow
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

class SpotMetaRow extends StatelessWidget {
  const SpotMetaRow({super.key, required this.spot});

  final Spot spot;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Wrap(
      spacing: 12,
      runSpacing: 4,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.star_rounded, size: 16, color: Colors.amber[700]),
            const SizedBox(width: 4),
            Text(spot.rating.toStringAsFixed(1),
                style: theme.textTheme.bodySmall),
          ],
        ),
        Text(
          r'$' * spot.priceLevel,
          style: theme.textTheme.bodySmall
              ?.copyWith(color: theme.colorScheme.onSurfaceVariant),
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
