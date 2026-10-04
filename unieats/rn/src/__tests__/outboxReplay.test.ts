import { randomUUID } from "expo-crypto";
import { getSpot, patchSpot } from "../api/client";
import { getDb } from "../db/database";
import * as outboxDao from "../db/outboxDao";
import { getSpotById, upsertFromServer } from "../db/spotDao";
import { editSpot, syncPending } from "../repository/spotRepository";
import { useSessionStore } from "../store/sessionStore";
import { FakeSpotServer } from "./support/fakeSpotServer";
import { spot, student } from "./support/fixtures";

jest.mock("expo-sqlite", () => jest.requireActual("./support/memorySqlite"));
jest.mock("expo-secure-store", () => jest.requireActual("./support/secureStoreFake"));
jest.mock("expo-crypto", () => ({ randomUUID: jest.fn() }));
jest.mock("../api/client", () => ({
  ...jest.requireActual("../api/client"),
  getSpot: jest.fn(),
  patchSpot: jest.fn(),
}));
jest.mock("../api/queryClient", () => ({ queryClient: { invalidateQueries: jest.fn() } }));

let server: FakeSpotServer;

const edit = (name: string) => ({ name, description: "d", openNow: true });
const row = (id: string) => getSpotById(id)!;
const sent = () => server.patches.map((p) => [p.id, p.body.name]);

beforeEach(() => {
  server = new FakeSpotServer();
  jest.mocked(patchSpot).mockImplementation(server.patchSpot);
  jest.mocked(getSpot).mockImplementation(server.getSpot);
  let ops = 0;
  jest.mocked(randomUUID).mockImplementation(() => `op-${++ops}`);
  jest.spyOn(console, "warn").mockImplementation(() => {});
  getDb().execSync("DELETE FROM outbox; DELETE FROM spots;");
  for (const id of ["spot-1", "spot-2"]) {
    server.put(spot(id));
    upsertFromServer([spot(id)]);
  }
  useSessionStore.setState({ status: "signedIn", token: "token-1", user: student, notice: null });
});

it("replays queued edits in order with the op id as Idempotency-Key, then marks the rows synced", async () => {
  editSpot("spot-1", edit("First"));
  editSpot("spot-2", edit("Second"));
  await syncPending();

  expect(sent()).toEqual([["spot-1", "First"], ["spot-2", "Second"]]);
  expect(server.patches.map((p) => [p.idempotencyKey, p.token, p.body.updatedAt])).toEqual([
    ["op-1", "token-1", 1_000],
    ["op-2", "token-1", 1_000],
  ]);
  expect(outboxDao.count()).toBe(0);
  expect(row("spot-1")).toMatchObject({ name: "First", pendingSync: false, updatedAt: server.spots.get("spot-1")!.updatedAt });
  expect(row("spot-2")).toMatchObject({ name: "Second", pendingSync: false });
});

it("keeps an edit made while the PATCH of the same spot is in flight and rebases it onto the accepted version", async () => {
  server.onPatch = () => {
    if (server.patches.length === 1) editSpot("spot-1", edit("Second"));
  };
  editSpot("spot-1", edit("First"));
  await syncPending();

  expect(sent()).toEqual([["spot-1", "First"], ["spot-1", "Second"]]);
  const firstAccepted = server.patches[1].body.updatedAt;
  expect(firstAccepted).toBeGreaterThan(1_000);
  expect(row("spot-1")).toMatchObject({ name: "Second", pendingSync: false, updatedAt: server.spots.get("spot-1")!.updatedAt });
  expect(server.spots.get("spot-1")!.name).toBe("Second");
  expect(outboxDao.count()).toBe(0);
});

it("re-checks the outbox after fetching a server copy, so an edit made meanwhile is not overwritten", async () => {
  let rowDuringSecondPatch: ReturnType<typeof getSpotById> = null;
  server.onGet = (id) => {
    if (server.gets === 1) editSpot(id, edit("Good"));
  };
  server.onPatch = (patch) => {
    if (patch.body.name === "Good") rowDuringSecondPatch = getSpotById(patch.id);
  };

  editSpot("spot-2", edit(""));
  await syncPending();

  expect(sent()).toEqual([["spot-2", ""], ["spot-2", "Good"]]);
  expect(rowDuringSecondPatch).toMatchObject({ name: "Good", pendingSync: true });
  expect(row("spot-2")).toMatchObject({ name: "Good", pendingSync: false });
});

it("on a 409 keeps the server copy and drops the queued edits of that spot based on the stale version", async () => {
  server.put(spot("spot-1", { name: "Changed on the server", updatedAt: 2_000 }));
  server.onPatch = () => {
    if (server.patches.length === 1) {
      editSpot("spot-1", edit("Second"));
      editSpot("spot-2", edit("Other spot"));
    }
  };

  editSpot("spot-1", edit("First"));
  await syncPending();

  expect(sent()).toEqual([["spot-1", "First"], ["spot-2", "Other spot"]]);
  expect(row("spot-1")).toMatchObject({ name: "Changed on the server", updatedAt: 2_000, pendingSync: false });
  expect(server.spots.get("spot-1")!.name).toBe("Changed on the server");
  expect(row("spot-2")).toMatchObject({ name: "Other spot", pendingSync: false });
  expect(outboxDao.count()).toBe(0);
});

it("sends nothing without a token and stops as soon as the token changes mid-run", async () => {
  useSessionStore.setState({ token: null });
  editSpot("spot-1", edit("A"));
  editSpot("spot-2", edit("B"));
  await syncPending();
  expect(server.patches).toHaveLength(0);

  useSessionStore.setState({ token: "token-1" });
  server.onPatch = () => useSessionStore.setState({ token: "token-2" });
  await syncPending();

  expect(server.patches.map((p) => [p.body.name, p.token])).toEqual([["A", "token-1"]]);
  expect(outboxDao.oldest()?.payload.name).toBe("B");
  expect(row("spot-2").pendingSync).toBe(true);
});

it("a 401 ends the session but keeps the op, which replays after signing in again", async () => {
  server.status = 401;
  editSpot("spot-1", edit("Offline edit"));
  await syncPending();

  expect(useSessionStore.getState()).toMatchObject({ status: "signedOut", token: null });
  expect(outboxDao.count()).toBe(1);
  expect(row("spot-1")).toMatchObject({ name: "Offline edit", pendingSync: true });

  server.status = null;
  useSessionStore.setState({ status: "signedIn", token: "token-2", user: student });
  await syncPending();

  expect(server.patches.map((p) => [p.body.name, p.token, p.idempotencyKey])).toEqual([
    ["Offline edit", "token-1", "op-1"],
    ["Offline edit", "token-2", "op-1"],
  ]);
  expect(row("spot-1").pendingSync).toBe(false);
});

it("keeps the op for a later retry when the server fails with 5xx", async () => {
  server.status = 503;
  editSpot("spot-1", edit("Later"));
  await syncPending();

  expect(outboxDao.count()).toBe(1);
  expect(row("spot-1")).toMatchObject({ name: "Later", pendingSync: true });
});
