import * as Notifications from "expo-notifications";
import { Platform } from "react-native";
import type { Spot } from "@unieats/shared";

const CHANNEL_ID = "spot-updates";

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: false,
    shouldSetBadge: false,
  }),
});

/** On Android 13+ this shows the POST_NOTIFICATIONS prompt; the channel must exist first. */
export async function requestNotificationPermission(): Promise<boolean> {
  if (Platform.OS === "android") {
    await Notifications.setNotificationChannelAsync(CHANNEL_ID, {
      name: "Spot updates",
      importance: Notifications.AndroidImportance.DEFAULT,
    });
  }
  const current = await Notifications.getPermissionsAsync();
  if (current.granted) return true;
  return (await Notifications.requestPermissionsAsync()).granted;
}

export async function notifySpotUpdated(spot: Spot) {
  await Notifications.scheduleNotificationAsync({
    content: {
      title: "Spot updated",
      body: `${spot.name} was updated`,
      data: { spotId: spot.id },
    },
    trigger: Platform.OS === "android" ? { channelId: CHANNEL_ID } : null,
  });
}

export function onNotificationTapped(open: (spotId: string) => void) {
  const sub = Notifications.addNotificationResponseReceivedListener((response) => {
    const spotId = response.notification.request.content.data?.spotId;
    if (typeof spotId === "string") open(spotId);
  });
  return () => sub.remove();
}
