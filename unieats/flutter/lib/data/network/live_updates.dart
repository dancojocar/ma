import 'dart:async';
import 'dart:convert';
import 'dart:io';

import '../../domain/models.dart';

sealed class LiveEvent {
  const LiveEvent();

  /// A malformed message is skipped, never fatal to the connection.
  static LiveEvent? fromMessage(Object? message) {
    if (message is! String) return null;
    try {
      return switch (jsonDecode(message)) {
        {
          'type': final String type && ('spot.created' || 'spot.updated'),
          'spot': final Map<String, dynamic> spot,
        } =>
          SpotChanged(Spot.fromJson(spot), created: type == 'spot.created'),
        {'type': 'spot.deleted', 'id': final String id} => SpotDeleted(id),
        _ => null,
      };
    } on Object {
      return null;
    }
  }
}

class LiveConnected extends LiveEvent {
  const LiveConnected();
}

class LiveDisconnected extends LiveEvent {
  const LiveDisconnected(this.retryIn);

  final Duration retryIn;
}

class SpotChanged extends LiveEvent {
  const SpotChanged(this.spot, {this.created = false});

  final Spot spot;
  final bool created;
}

class SpotDeleted extends LiveEvent {
  const SpotDeleted(this.id);

  final String id;
}

/// Listens to the server's `/live` WebSocket. When the connection fails or
/// drops it emits [LiveDisconnected] and reconnects with exponential backoff
/// (1 s doubling up to 30 s). Cancelling the subscription closes the socket.
Stream<LiveEvent> liveSpotEvents(Uri url) {
  late final StreamController<LiveEvent> controller;
  WebSocket? socket;
  var cancelled = false;

  Future<void> connectLoop() async {
    var backoff = const Duration(seconds: 1);
    while (!cancelled) {
      try {
        socket = await WebSocket.connect(
          url.toString(),
        ).timeout(const Duration(seconds: 10));
        if (cancelled) break;
        controller.add(const LiveConnected());
        backoff = const Duration(seconds: 1);
        await for (final message in socket!) {
          final event = LiveEvent.fromMessage(message);
          if (event != null) controller.add(event);
        }
      } on Exception {
        // Connection refused, timed out or dropped: fall through to the retry.
      }
      if (cancelled) break;
      controller.add(LiveDisconnected(backoff));
      await Future<void>.delayed(backoff);
      backoff =
          backoff * 2 > const Duration(seconds: 30)
              ? const Duration(seconds: 30)
              : backoff * 2;
    }
    await socket?.close();
  }

  controller = StreamController<LiveEvent>(
    onListen: connectLoop,
    onCancel: () {
      cancelled = true;
      return socket?.close();
    },
  );
  return controller.stream;
}
