import { resolveConflict } from "@unieats/shared";

describe("resolveConflict (last-write-wins on updatedAt, CONTRACT §5)", () => {
  it("server wins when the server copy is strictly newer", () => {
    expect(resolveConflict(1_000, 2_000)).toBe("server");
  });

  it("client wins when the local copy is newer", () => {
    expect(resolveConflict(2_000, 1_000)).toBe("client");
  });

  it("client wins on a tie", () => {
    expect(resolveConflict(1_500, 1_500)).toBe("client");
  });

  it("server wins by a single millisecond", () => {
    expect(resolveConflict(1_700_000_000_000, 1_700_000_000_001)).toBe("server");
  });
});
