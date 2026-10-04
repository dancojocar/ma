import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:unieats_data/unieats_data.dart';

import '../../data/network/app_error.dart';
import '../../providers/providers.dart';

/// "Describe this dish": streams the server's AI description chunk by chunk.
class DescribeDishCard extends ConsumerStatefulWidget {
  const DescribeDishCard({super.key, required this.spot});

  final Spot spot;

  @override
  ConsumerState<DescribeDishCard> createState() => _DescribeDishCardState();
}

class _DescribeDishCardState extends ConsumerState<DescribeDishCard> {
  StreamSubscription<String>? _subscription;
  String _text = '';
  String? _error;
  bool _streaming = false;

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  void _describe() {
    _subscription?.cancel();
    setState(() {
      _text = '';
      _error = null;
      _streaming = true;
    });
    _subscription = ref
        .read(apiClientProvider)
        .describeSpot(widget.spot)
        .listen(
          (delta) => setState(() => _text += delta),
          onError:
              (Object e) => setState(() {
                _error = _messageFor(e);
                _streaming = false;
              }),
          onDone: () => setState(() => _streaming = false),
        );
  }

  String _messageFor(Object error) => switch (error) {
    NoConnectivity() ||
    TimeoutError() => 'Server unreachable. Is the UniEats server running?',
    HttpError(statusCode: 429) => 'Too many requests: try again in a minute.',
    AppError(:final message) => message,
    _ => '$error',
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        OutlinedButton.icon(
          onPressed: _streaming ? null : _describe,
          icon:
              _streaming
                  ? const SizedBox.square(
                    dimension: 16,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                  : const Icon(Icons.auto_awesome_outlined, size: 18),
          label: const Text('Describe this dish'),
        ),
        if (_text.isNotEmpty || _error != null) ...[
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color:
                  _error == null
                      ? theme.colorScheme.secondaryContainer
                      : theme.colorScheme.errorContainer,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(_error ?? _text),
          ),
        ],
      ],
    );
  }
}
