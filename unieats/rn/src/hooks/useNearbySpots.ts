import * as Location from "expo-location";
import { useFocusEffect } from "expo-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocalSpots } from "../db/hooks";
import { distanceKm, NEARBY_RADIUS_KM } from "../domain/geo";
import type { LocalSpot } from "../domain/models";

export type PermissionState = "unknown" | "granted" | "denied" | "blocked";

export interface NearbySpot {
  spot: LocalSpot;
  distanceKm: number;
}

/** Watches the position only while the calling screen is focused; the watch is removed on blur/unmount. */
export function useNearbySpots() {
  const [permission, setPermission] = useState<PermissionState>("unknown");
  const [coords, setCoords] = useState<Location.LocationObjectCoords | null>(null);
  const spots = useLocalSpots("", "all");

  useEffect(() => {
    Location.getForegroundPermissionsAsync().then((p) => setPermission(toState(p)));
  }, []);

  const requestPermission = useCallback(async () => {
    setPermission(toState(await Location.requestForegroundPermissionsAsync()));
  }, []);

  useFocusEffect(
    useCallback(() => {
      if (permission !== "granted") return;
      let subscription: Location.LocationSubscription | null = null;
      let cancelled = false;
      Location.watchPositionAsync(
        { accuracy: Location.Accuracy.Balanced, distanceInterval: 25 },
        (location) => setCoords(location.coords),
      ).then((sub) => {
        if (cancelled) sub.remove();
        else subscription = sub;
      });
      return () => {
        cancelled = true;
        subscription?.remove();
      };
    }, [permission]),
  );

  const nearby = useMemo<NearbySpot[]>(() => {
    if (!coords) return [];
    return spots
      .map((spot) => ({ spot, distanceKm: distanceKm(coords.latitude, coords.longitude, spot.lat, spot.lng) }))
      .filter((n) => n.distanceKm <= NEARBY_RADIUS_KM)
      .sort((a, b) => a.distanceKm - b.distanceKm);
  }, [coords, spots]);

  return { permission, requestPermission, located: coords !== null, nearby };
}

function toState(p: Location.LocationPermissionResponse): PermissionState {
  if (p.granted) return "granted";
  if (p.status === Location.PermissionStatus.UNDETERMINED) return "unknown";
  return p.canAskAgain ? "denied" : "blocked";
}
