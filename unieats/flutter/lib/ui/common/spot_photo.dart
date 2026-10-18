import 'package:material_ui/material_ui.dart';

class SpotPhoto extends StatelessWidget {
  const SpotPhoto({
    super.key,
    required this.url,
    required this.height,
    this.semanticLabel,
  });

  final String url;
  final double height;
  final String? semanticLabel;

  @override
  Widget build(BuildContext context) {
    final placeholder = _Placeholder(
      height: height,
      semanticLabel: semanticLabel,
    );
    if (url.isEmpty) return placeholder;
    return Image.network(
      url,
      height: height,
      width: double.infinity,
      fit: BoxFit.cover,
      semanticLabel: semanticLabel,
      excludeFromSemantics: semanticLabel == null,
      loadingBuilder: (context, child, progress) =>
          progress == null ? child : placeholder,
      errorBuilder: (context, error, stackTrace) => placeholder,
    );
  }
}

class _Placeholder extends StatelessWidget {
  const _Placeholder({required this.height, this.semanticLabel});

  final double height;
  final String? semanticLabel;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      height: height,
      width: double.infinity,
      color: scheme.surfaceContainerHighest,
      child: Icon(
        Icons.restaurant,
        size: 48,
        color: scheme.onSurfaceVariant,
        semanticLabel: semanticLabel,
      ),
    );
  }
}
