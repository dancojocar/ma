import Constants from "expo-constants";
import { Platform } from "react-native";

// The Android emulator reaches the host machine's localhost as 10.0.2.2.
const FALLBACK_HOST = Platform.OS === "android" ? "10.0.2.2" : "localhost";

function metroHost(): string | null {
  const host = Constants.expoConfig?.hostUri?.split(":")[0];
  if (!host || host === "localhost" || host === "127.0.0.1" || host.endsWith(".exp.direct")) {
    return null;
  }
  return host;
}

const DEV_HOST = (__DEV__ && metroHost()) || FALLBACK_HOST;

export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? `http://${DEV_HOST}:3000/api`;
