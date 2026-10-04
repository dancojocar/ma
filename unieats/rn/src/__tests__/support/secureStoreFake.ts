/**
 * Stand-in for expo-secure-store with the behaviour the app relies on: items are separated by
 * `keychainService`, and an item saved with `requireAuthentication` can only be read through a biometric
 * prompt, whose outcome the test chooses. After an enrollment change such an item reads as null, like the
 * invalidated Keystore key / `.biometryCurrentSet` item on a device.
 */
interface Options {
  keychainService?: string;
  requireAuthentication?: boolean;
}

export const WHEN_UNLOCKED_THIS_DEVICE_ONLY = 6;

export const keychain = {
  items: new Map<string, string>(),
  biometricsAvailable: true,
  prompts: 0,
  promptResult: "match" as "match" | "cancel",
  enrollmentChanged: false,
  reset() {
    this.items.clear();
    this.biometricsAvailable = true;
    this.prompts = 0;
    this.promptResult = "match";
    this.enrollmentChanged = false;
  },
  has(key: string, options: Options = {}) {
    return this.items.has(slot(key, options));
  },
};

const slot = (key: string, options: Options) => `${options.keychainService ?? "default"}/${key}`;

export async function setItemAsync(key: string, value: string, options: Options = {}) {
  keychain.items.set(slot(key, options), value);
}

export async function getItemAsync(key: string, options: Options = {}): Promise<string | null> {
  const value = keychain.items.get(slot(key, options)) ?? null;
  if (!options.requireAuthentication || value === null) return value;
  keychain.prompts += 1;
  if (keychain.promptResult === "cancel") throw new Error("User canceled the authentication");
  return keychain.enrollmentChanged ? null : value;
}

export async function deleteItemAsync(key: string, options: Options = {}) {
  keychain.items.delete(slot(key, options));
}

export function canUseBiometricAuthentication() {
  return keychain.biometricsAvailable;
}
