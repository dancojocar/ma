import 'package:flutter/material.dart';

import '../../domain/models.dart';

class SpotSearchBar extends StatelessWidget {
  const SpotSearchBar({
    super.key,
    required this.query,
    required this.onQueryChange,
  });

  final String query;
  final ValueChanged<String> onQueryChange;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
      child: TextFormField(
        initialValue: query,
        onChanged: onQueryChange,
        textInputAction: TextInputAction.search,
        decoration: const InputDecoration(
          hintText: 'Search spots',
          prefixIcon: Icon(Icons.search),
          border: OutlineInputBorder(),
          isDense: true,
        ),
      ),
    );
  }
}

class CategoryFilterRow extends StatelessWidget {
  const CategoryFilterRow({
    super.key,
    required this.selected,
    required this.onSelected,
  });

  final SpotCategory? selected;
  final ValueChanged<SpotCategory?> onSelected;

  @override
  Widget build(BuildContext context) {
    final options = <SpotCategory?>[null, ...SpotCategory.values];
    return SizedBox(
      height: 48,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        itemCount: options.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final category = options[index];
          return ChoiceChip(
            label: Text(category?.label ?? 'All'),
            selected: selected == category,
            onSelected: (_) => onSelected(category),
          );
        },
      ),
    );
  }
}

class EmptySpotsMessage extends StatelessWidget {
  const EmptySpotsMessage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.search_off_rounded,
            size: 64,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          const SizedBox(height: 12),
          Text('No spots found', style: theme.textTheme.titleMedium),
          const SizedBox(height: 4),
          const Text('Try another search or category.'),
        ],
      ),
    );
  }
}

class LiveIndicator extends StatelessWidget {
  const LiveIndicator({super.key, required this.connected});

  final bool connected;

  @override
  Widget build(BuildContext context) {
    final color = connected ? Colors.green : Colors.grey;
    return Tooltip(
      message: connected ? 'Live updates on' : 'Reconnecting to live updates',
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12),
        child: Row(
          children: [
            Icon(Icons.circle, size: 10, color: color),
            const SizedBox(width: 4),
            Text(
              connected ? 'Live' : 'Offline',
              style: TextStyle(color: color),
            ),
          ],
        ),
      ),
    );
  }
}
