import { act, renderHook } from "@testing-library/react-native";
import { AccessibilityInfo } from "react-native";
import { streamDishDescription } from "../api/aiClient";
import { ApiError } from "../api/errors";
import { useDishDescription } from "../hooks/useDishDescription";
import { useSessionStore } from "../store/sessionStore";
import { spot } from "./support/fixtures";

jest.mock("../api/aiClient", () => ({ streamDishDescription: jest.fn() }));
jest.mock("../store/sessionStore", () => ({ useSessionStore: { getState: jest.fn() } }));

const stream = jest.mocked(streamDishDescription);
const expire = jest.fn(async () => {});
let announce: jest.SpyInstance;

function session(status: string, token: string | null) {
  jest.mocked(useSessionStore.getState).mockReturnValue({ status, token, expire } as never);
}

const pizzaStop = spot("spot-3", { name: "Pizza Stop" });

async function describeDish() {
  const hook = await renderHook(() => useDishDescription(pizzaStop));
  await act(() => hook.result.current.describe());
  return hook;
}

beforeEach(() => {
  jest.clearAllMocks();
  announce = jest.spyOn(AccessibilityInfo, "announceForAccessibility").mockImplementation(() => {});
  session("signedIn", "token-1");
});

it("shows the text chunk by chunk and announces the whole description once at the end", async () => {
  let finish!: () => void;
  stream.mockImplementation(async (_spot, _token, onDelta) => {
    onDelta("Pizza Stop ");
    await new Promise<void>((resolve) => (finish = resolve));
    onDelta("is open.");
    return "template";
  });
  const { result } = await renderHook(() => useDishDescription(pizzaStop));

  let done!: Promise<void>;
  await act(async () => {
    done = result.current.describe();
  });
  expect(result.current).toMatchObject({ text: "Pizza Stop ", status: "streaming", error: null });
  expect(announce).not.toHaveBeenCalled();

  await act(async () => {
    finish();
    await done;
  });
  expect(result.current).toMatchObject({ text: "Pizza Stop is open.", status: "done", source: "template" });
  expect(stream).toHaveBeenCalledWith(pizzaStop, "token-1", expect.any(Function), expect.any(AbortSignal));
  expect(announce).toHaveBeenCalledTimes(1);
  expect(announce).toHaveBeenCalledWith("Pizza Stop is open.");
});

it.each([
  ["locked", null],
  ["signedOut", null],
  ["signedIn", null],
])("sends nothing while %s without a token", async (status, token) => {
  session(status, token);
  const { result } = await describeDish();
  expect(stream).not.toHaveBeenCalled();
  expect(result.current.status).toBe("idle");
});

it("a 401 ends the session instead of showing an error", async () => {
  stream.mockRejectedValue(new ApiError("http", "Unauthorized", 401, "unauthorized"));
  const { result } = await describeDish();
  expect(expire).toHaveBeenCalledTimes(1);
  expect(result.current.error).toBeNull();
  expect(announce).not.toHaveBeenCalled();
});

it.each([
  [new ApiError("http", "HTTP 429", 429, "rate_limited"), "Too many requests. Try again in a minute."],
  [new ApiError("network", "Network request failed"), "Server unreachable. Is the UniEats server running?"],
  [new ApiError("timeout", "POST /ai/describe timed out"), "Server unreachable. Is the UniEats server running?"],
  [new ApiError("http", "The AI service is unavailable, try again later", 503, "ai_unavailable"), "The AI service is unavailable, try again later"],
  [new ApiError("parse", "The stream ended before the done event"), "The server sent an unexpected response."],
])("shows and announces a readable error for %s", async (error, message) => {
  stream.mockRejectedValue(error);
  const { result } = await describeDish();
  expect(result.current).toMatchObject({ status: "error", error: message });
  expect(announce).toHaveBeenCalledWith(message);
});

it("leaving the screen aborts the stream quietly", async () => {
  let signal!: AbortSignal;
  stream.mockImplementation(
    (_spot, _token, onDelta, abortSignal) =>
      new Promise((_resolve, reject) => {
        signal = abortSignal!;
        onDelta("Pizza");
        signal.addEventListener("abort", () => reject(new ApiError("network", "Aborted")));
      }),
  );
  const { result, unmount } = await renderHook(() => useDishDescription(pizzaStop));
  let done!: Promise<void>;
  await act(async () => {
    done = result.current.describe();
  });

  await unmount();
  await done;

  expect(signal.aborted).toBe(true);
  expect(announce).not.toHaveBeenCalled();
});
