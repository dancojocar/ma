import 'package:flutter/material.dart';

class ErrorView extends StatelessWidget {
  const ErrorView({super.key, required this.error, required this.onRetry});

  final Object error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.cloud_off, size: 56, color: theme.colorScheme.error),
            const SizedBox(height: 12),
            Text('Something went wrong', style: theme.textTheme.titleMedium),
            const SizedBox(height: 4),
            Text('$error', textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton.tonal(onPressed: onRetry, child: const Text('Retry')),
          ],
        ),
      ),
    );
  }
}

class SpotPhoto extends StatelessWidget {
  const SpotPhoto({super.key, required this.url, required this.height});

  final String url;
  final double height;

  @override
  Widget build(BuildContext context) {
    final placeholder = _Placeholder(height: height);
    if (url.isEmpty) return placeholder;
    return Image.network(
      url,
      height: height,
      width: double.infinity,
      fit: BoxFit.cover,
      loadingBuilder:
          (context, child, progress) => progress == null ? child : placeholder,
      errorBuilder: (_, _, _) => placeholder,
    );
  }
}

class _Placeholder extends StatelessWidget {
  const _Placeholder({required this.height});

  final double height;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      height: height,
      width: double.infinity,
      color: scheme.surfaceContainerHighest,
      child: Icon(Icons.restaurant, size: 48, color: scheme.onSurfaceVariant),
    );
  }
}
