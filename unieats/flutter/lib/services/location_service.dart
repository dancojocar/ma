import 'package:geolocator/geolocator.dart';

import 'package:unieats_data/unieats_data.dart';

enum LocationAccess { granted, denied, deniedForever, serviceDisabled }

class LocationService {
  static const nearbyRadiusMeters = 2000.0;

  Future<LocationAccess> checkAccess({bool request = false}) async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      return LocationAccess.serviceDisabled;
    }
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied && request) {
      permission = await Geolocator.requestPermission();
    }
    return switch (permission) {
      LocationPermission.always ||
      LocationPermission.whileInUse => LocationAccess.granted,
      LocationPermission.deniedForever => LocationAccess.deniedForever,
      _ => LocationAccess.denied,
    };
  }

  /// Position updates every 25 m. Cancelling the subscription stops the GPS.
  Stream<Position> positions() => Geolocator.getPositionStream(
    locationSettings: const LocationSettings(
      accuracy: LocationAccuracy.high,
      distanceFilter: 25,
    ),
  );

  Future<bool> openSettings() => Geolocator.openAppSettings();

  List<({Spot spot, double meters})> nearby(Position here, List<Spot> spots) {
    final withDistance = [
      for (final spot in spots)
        (
          spot: spot,
          meters: Geolocator.distanceBetween(
            here.latitude,
            here.longitude,
            spot.lat,
            spot.lng,
          ),
        ),
    ];
    return withDistance.where((e) => e.meters <= nearbyRadiusMeters).toList()
      ..sort((a, b) => a.meters.compareTo(b.meters));
  }
}
