import Constants, { ExecutionEnvironment } from "expo-constants";
import * as LocalAuthentication from "expo-local-authentication";
import * as SecureStore from "expo-secure-store";
import { Platform } from "react-native";

export interface BiometricSupport {
  available: boolean;
  label: string;
}

function labelFor(types: LocalAuthentication.AuthenticationType[]): string {
  const { FACIAL_RECOGNITION, FINGERPRINT } = LocalAuthentication.AuthenticationType;
  if (Platform.OS === "ios") return types.includes(FACIAL_RECOGNITION) ? "Face ID" : "Touch ID";
  return types.length === 1 && types[0] === FINGERPRINT ? "fingerprint" : "biometrics";
}

/**
 * Strong biometrics enrolled and usable for a protected SecureStore item. Not offered in Expo Go, which
 * lacks this app's Face ID usage description.
 */
export async function biometricSupport(): Promise<BiometricSupport> {
  const label = labelFor(await LocalAuthentication.supportedAuthenticationTypesAsync());
  if (Constants.executionEnvironment === ExecutionEnvironment.StoreClient) return { available: false, label };
  const available =
    SecureStore.canUseBiometricAuthentication() &&
    (await LocalAuthentication.hasHardwareAsync()) &&
    (await LocalAuthentication.isEnrolledAsync());
  return { available, label };
}

/** iOS writes a protected item without prompting, so turning the feature on asks for biometrics explicitly. */
export async function confirmBiometricOwner(): Promise<boolean> {
  if (Platform.OS !== "ios") return true;
  const result = await LocalAuthentication.authenticateAsync({
    promptMessage: "Turn on biometric unlock",
    disableDeviceFallback: true,
  });
  return result.success;
}
