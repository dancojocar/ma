import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';
import 'package:unieats_data/unieats_data.dart';

import '../../data/network/app_error.dart';
import '../../providers/providers.dart';

/// "Describe this dish": streams the server's AI description chunk by chunk.
class DescribeDishCard extends ConsumerWidget {
  const DescribeDishCard({super.key, required this.spot});

  final Spot spot;

  static String _messageFor(Object error) => switch (error) {
    NoConnectivity() ||
    TimeoutError() => 'Server unreachable. Is the UniEats server running?',
    HttpError(statusCode: 429) => 'Too many requests: try again in a minute.',
    AppError(:final message) => message,
    _ => '$error',
  };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = Theme.of(context).colorScheme;
    final description = ref.watch(describeDishProvider(spot.id));
    final error = description.error;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Semantics(
          identifier: 'describe-dish-button',
          container: true,
          child: OutlinedButton.icon(
            onPressed: description.isStreaming
                ? null
                : () => ref
                      .read(describeDishProvider(spot.id).notifier)
                      .describe(spot),
            icon: description.isStreaming
                ? const SizedBox.square(
                    dimension: 16,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      semanticsLabel: 'Describing',
                    ),
                  )
                : const Icon(Icons.auto_awesome_outlined, size: 18),
            label: const Text('Describe this dish'),
          ),
        ),
        if (description.text.isNotEmpty || error != null) ...[
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: error == null
                  ? colors.secondaryContainer
                  : colors.errorContainer,
              borderRadius: BorderRadius.circular(8),
            ),
            // Announced once, when the text is final, not on every chunk.
            child: Semantics(
              identifier: 'describe-dish-result',
              container: true,
              liveRegion: !description.isStreaming,
              child: Text(
                error == null ? description.text : _messageFor(error),
                style: TextStyle(
                  color: error == null
                      ? colors.onSecondaryContainer
                      : colors.onErrorContainer,
                ),
              ),
            ),
          ),
        ],
      ],
    );
  }
}
