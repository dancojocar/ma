import 'dart:async';
import 'dart:io';

import 'package:unieats_data/unieats_data.dart';

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
