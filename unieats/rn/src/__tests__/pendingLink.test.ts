import { redirectSystemPath } from "../../app/+native-intent";
import { forgetPendingLink, rememberPendingLink, spotIdFromLink, takePendingLink } from "../auth/pendingLink";
import { useSessionStore } from "../store/sessionStore";

jest.mock("../store/sessionStore", () => ({ useSessionStore: { getState: jest.fn() } }));

const sessionStatus = (status: string) => jest.mocked(useSessionStore.getState).mockReturnValue({ status } as never);

beforeEach(forgetPendingLink);

describe("spotIdFromLink", () => {
  it.each([
    ["unieats://spots/spot-1", "spot-1"],
    ["unieats:///spots/spot-1", "spot-1"],
    ["https://unieats.app/spots/spot-1", "spot-1"],
    ["https://unieats.app/spots/spot-1/", "spot-1"],
    ["/spots/spot-1", "spot-1"],
    ["spots/spot-1?utm=mail#top", "spot-1"],
    ["/spots/caf%C3%A9", "café"],
  ])("%s -> %s", (url, id) => {
    expect(spotIdFromLink(url)).toBe(id);
  });

  it.each(["/login", "/spots", "/spots/a/b", "https://evil.example/spots/spot-1", "unieats://profile", "/spots/%E0%A4%A"])(
    "ignores %s",
    (url) => {
      expect(spotIdFromLink(url)).toBeNull();
    },
  );
});

describe("pending link", () => {
  it("is taken once, as the detail route of that spot", () => {
    rememberPendingLink("https://unieats.app/spots/spot-3");
    expect(takePendingLink()).toEqual({ pathname: "/spots/[id]", params: { id: "spot-3" } });
    expect(takePendingLink()).toBeNull();
  });

  it("keeps the last spot link and ignores links to other screens", () => {
    rememberPendingLink("unieats://spots/spot-1");
    rememberPendingLink("unieats://spots/spot-2");
    rememberPendingLink("/login");
    expect(takePendingLink()).toEqual({ pathname: "/spots/[id]", params: { id: "spot-2" } });
  });

  it("can be forgotten", () => {
    rememberPendingLink("unieats://spots/spot-1");
    forgetPendingLink();
    expect(takePendingLink()).toBeNull();
  });
});

describe("+native-intent redirectSystemPath", () => {
  it.each(["signedOut", "locked", "restoring"])("remembers a spot link while %s and routes it unchanged", (status) => {
    sessionStatus(status);
    expect(redirectSystemPath({ path: "unieats://spots/spot-5", initial: true })).toBe("unieats://spots/spot-5");
    expect(takePendingLink()).toEqual({ pathname: "/spots/[id]", params: { id: "spot-5" } });
  });

  it("does not remember anything when signed in", () => {
    sessionStatus("signedIn");
    expect(redirectSystemPath({ path: "unieats://spots/spot-5", initial: false })).toBe("unieats://spots/spot-5");
    expect(takePendingLink()).toBeNull();
  });
});
