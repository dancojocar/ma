import { resolveConflict } from "@unieats/shared";

describe("resolveConflict(base version of the edit, server version)", () => {
  it("server wins when the server copy is strictly newer", () => {
    expect(resolveConflict(1_000, 2_000)).toBe("server");
  });

  it("client wins when the edit is based on a newer version", () => {
    expect(resolveConflict(2_000, 1_000)).toBe("client");
  });

  it("client wins on a tie", () => {
    expect(resolveConflict(1_500, 1_500)).toBe("client");
  });

  it("server wins by a single millisecond", () => {
    expect(resolveConflict(1_700_000_000_000, 1_700_000_000_001)).toBe("server");
  });
});
