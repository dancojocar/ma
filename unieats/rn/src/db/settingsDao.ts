import { getDb } from "./database";

export function readSetting(key: string): string | null {
  return getDb().getFirstSync<{ value: string }>("SELECT value FROM settings WHERE key = ?", [key])?.value ?? null;
}

export function writeSetting(key: string, value: string) {
  getDb().runSync("INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)", [key, value]);
}
