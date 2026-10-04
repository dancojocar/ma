import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// Local notifications only: they are triggered by the live WebSocket while
/// the app runs, not by a push service.
class NotificationService {
  final _plugin = FlutterLocalNotificationsPlugin();
  Future<void>? _ready;

  static const _channel = AndroidNotificationDetails(
    'spot_updates',
    'Spot updates',
    channelDescription: 'A spot you can see in UniEats was changed',
  );

  /// Initialises the plugin and asks for permission (Android 13+
  /// POST_NOTIFICATIONS, iOS alert/badge/sound) once.
  Future<void> init() => _ready ??= _init();

  Future<void> _init() async {
    await _plugin.initialize(
      const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
        iOS: DarwinInitializationSettings(),
      ),
    );
    await _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.requestNotificationsPermission();
  }

  Future<void> showSpotUpdated(String spotId, String spotName) async {
    await init();
    await _plugin.show(
      spotId.hashCode,
      'Spot updated',
      '$spotName was updated',
      const NotificationDetails(
        android: _channel,
        iOS: DarwinNotificationDetails(),
      ),
      payload: spotId,
    );
  }
}
