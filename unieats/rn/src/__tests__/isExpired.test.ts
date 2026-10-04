import { isExpired } from "../auth/sessionStorage";
import { jwt } from "./support/fixtures";

jest.mock("expo-secure-store", () => jest.requireActual("./support/secureStoreFake"));

const NOW = 1_700_000_000_000;

describe("isExpired (reads exp from the JWT payload without verifying it)", () => {
  it("is false before exp", () => {
    expect(isExpired(jwt({ exp: NOW / 1000 + 60 }), NOW)).toBe(false);
  });

  it("is true at and after exp", () => {
    expect(isExpired(jwt({ exp: NOW / 1000 }), NOW)).toBe(true);
    expect(isExpired(jwt({ exp: NOW / 1000 - 1 }), NOW)).toBe(true);
  });

  it("decodes base64url payloads (- and _ instead of + and /)", () => {
    const token = jwt({ exp: NOW / 1000 + 60, name: "ÿÿÿ>>>???" });
    expect(token.split(".")[1]).toMatch(/[-_]/);
    expect(isExpired(token, NOW)).toBe(false);
  });

  it.each([
    ["no exp claim", jwt({ sub: "user-1" })],
    ["a non-numeric exp", jwt({ exp: "tomorrow" })],
    ["a payload that is not JSON", "header.bm90IGpzb24.signature"],
    ["a token without a payload", "garbage"],
  ])("treats %s as expired", (_case, token) => {
    expect(isExpired(token, NOW)).toBe(true);
  });
});
