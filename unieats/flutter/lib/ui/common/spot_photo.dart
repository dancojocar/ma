import 'package:flutter/material.dart';

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
      loadingBuilder: (context, child, progress) =>
          progress == null ? child : placeholder,
      errorBuilder: (context, error, stackTrace) => placeholder,
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
