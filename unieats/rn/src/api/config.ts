import { Platform } from "react-native";

// The Android emulator reaches the host machine's localhost as 10.0.2.2.
const DEV_HOST = Platform.OS === "android" ? "10.0.2.2" : "localhost";

export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? `http://${DEV_HOST}:3000/api`;
