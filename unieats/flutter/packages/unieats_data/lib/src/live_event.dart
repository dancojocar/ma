import 'dart:convert';

import 'models.dart';

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
