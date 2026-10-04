import type { Spot, User } from "@unieats/shared";

export const student: User = { id: "user-1", email: "student@unieats.app", displayName: "Demo Student" };
export const otherStudent: User = { id: "user-2", email: "other@unieats.app", displayName: "Other Student" };

export function spot(id: string, overrides: Partial<Spot> = {}): Spot {
  return {
    id,
    name: id,
    category: "cafe",
    rating: 4,
    priceLevel: 1,
    lat: 44.4268,
    lng: 26.1025,
    openNow: true,
    photoUrl: "",
    description: "d",
    updatedAt: 1_000,
    ...overrides,
  };
}

/** An unsigned JWT whose payload is `claims`; the app only reads `exp` from it. */
export function jwt(claims: Record<string, unknown>): string {
  const base64url = btoa(JSON.stringify(claims)).replace(/=+$/, "").replace(/\+/g, "-").replace(/\//g, "_");
  return `header.${base64url}.signature`;
}

export const tokenExpiringIn = (ms: number, nowMs = Date.now()) => jwt({ sub: "user-1", exp: Math.floor((nowMs + ms) / 1000) });
