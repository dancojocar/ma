import * as LocalAuthentication from "expo-local-authentication";
import { login as apiLogin } from "../api/client";
import { queryClient } from "../api/queryClient";
import { forgetPendingLink, rememberPendingLink, takePendingLink } from "../auth/pendingLink";
import { getDb } from "../db/database";
import * as outboxDao from "../db/outboxDao";
import { applyLocalEdit, getSpotById, upsertFromServer } from "../db/spotDao";
import { useSessionStore } from "../store/sessionStore";
import { otherStudent, spot, student, tokenExpiringIn } from "./support/fixtures";
import { keychain } from "./support/secureStoreFake";

jest.mock("expo-sqlite", () => jest.requireActual("./support/memorySqlite"));
jest.mock("expo-secure-store", () => jest.requireActual("./support/secureStoreFake"));
jest.mock("expo-local-authentication", () => ({
  AuthenticationType: { FINGERPRINT: 1, FACIAL_RECOGNITION: 2, IRIS: 3 },
  authenticateAsync: jest.fn(),
  hasHardwareAsync: jest.fn(),
  isEnrolledAsync: jest.fn(),
  supportedAuthenticationTypesAsync: jest.fn(),
}));
jest.mock("../api/client", () => ({ login: jest.fn() }));
jest.mock("../api/queryClient", () => ({ queryClient: { invalidateQueries: jest.fn() } }));

const PLAIN = { key: "unieats.session" };
const FLAG = { key: "unieats.session.biometric" };
const PROTECTED = { key: "unieats.biometricSession", options: { keychainService: "unieats.biometric" } };
const stored = (item: { key: string; options?: { keychainService: string } }) => keychain.has(item.key, item.options);

const EXPIRED_NOTICE = "Your session expired. Please sign in again.";
const BIOMETRICS_CHANGED_NOTICE = "Biometrics on this device changed. Sign in with your password.";

const store = () => useSessionStore.getState();
const validToken = tokenExpiringIn(60 * 60 * 1000);

function queueEdit(spotId: string, userId: string | null) {
  upsertFromServer([spot(spotId)]);
  applyLocalEdit(spotId, { name: `edited by ${userId}`, description: "d", openNow: true });
  outboxDao.enqueue({
    opId: `op-${spotId}`,
    type: "update",
    entityId: spotId,
    userId,
    payload: { name: `edited by ${userId}`, description: "d", openNow: true, updatedAt: 1_000 },
    createdAt: Date.now(),
  });
}

const queuedSpotIds = () =>
  getDb().getAllSync<{ entityId: string }>("SELECT entityId FROM outbox ORDER BY seq").map((op) => op.entityId);

/** What a cold start sees: an empty in-memory store, then `restore()` from SecureStore. */
async function coldStart() {
  useSessionStore.setState(useSessionStore.getInitialState(), true);
  await store().restore();
}

async function signIn(user = student, token = validToken) {
  jest.mocked(apiLogin).mockResolvedValueOnce({ token, user });
  await store().login(user.email, "password");
}

async function signInWithBiometricUnlock() {
  await signIn();
  await store().setBiometric(true);
  await coldStart();
}

beforeEach(() => {
  keychain.reset();
  jest.clearAllMocks();
  jest.mocked(LocalAuthentication.authenticateAsync).mockResolvedValue({ success: true });
  forgetPendingLink();
  getDb().execSync("DELETE FROM outbox; DELETE FROM spots;");
  useSessionStore.setState(useSessionStore.getInitialState(), true);
});

describe("restore at start-up", () => {
  it("starts in `restoring`", () => {
    expect(store().status).toBe("restoring");
  });

  it("is signed out without a stored session", async () => {
    await coldStart();
    expect(store()).toMatchObject({ status: "signedOut", token: null, user: null, notice: null });
  });

  it("signs in with a stored, unexpired session and drops a pending link (the launch URL was already routed)", async () => {
    await signIn();
    rememberPendingLink("unieats://spots/spot-9");
    await coldStart();
    expect(store()).toMatchObject({ status: "signedIn", token: validToken, user: student, biometricEnabled: false });
    expect(takePendingLink()).toBeNull();
  });

  it("deletes an expired session and says so", async () => {
    await signIn(student, tokenExpiringIn(-1_000));
    await coldStart();
    expect(store()).toMatchObject({ status: "signedOut", token: null, notice: EXPIRED_NOTICE });
    expect(stored(PLAIN)).toBe(false);
  });

  it("is locked when biometric unlock is on, without prompting yet", async () => {
    await signInWithBiometricUnlock();
    expect(store()).toMatchObject({ status: "locked", token: null, biometricEnabled: true });
    expect(keychain.prompts).toBe(0);
  });
});

describe("biometric unlock", () => {
  it("turning it on moves the session into the protected item (after confirming with biometrics on iOS)", async () => {
    await signIn();
    await store().setBiometric(true);
    expect(LocalAuthentication.authenticateAsync).toHaveBeenCalledTimes(1);
    expect(store().biometricEnabled).toBe(true);
    expect(stored(PROTECTED)).toBe(true);
    expect(stored(FLAG)).toBe(true);
    expect(stored(PLAIN)).toBe(false);
  });

  it("stays off when the confirmation fails", async () => {
    jest.mocked(LocalAuthentication.authenticateAsync).mockResolvedValue({ success: false, error: "user_cancel" });
    await signIn();
    await store().setBiometric(true);
    expect(store().biometricEnabled).toBe(false);
    expect(stored(PROTECTED)).toBe(false);
    expect(stored(PLAIN)).toBe(true);
  });

  it("unlock reads the protected item through the prompt and signs in", async () => {
    await signInWithBiometricUnlock();
    await store().unlock();
    expect(keychain.prompts).toBe(1);
    expect(store()).toMatchObject({ status: "signedIn", token: validToken, user: student, biometricEnabled: true });
  });

  it("a cancelled prompt rejects and stays locked, so the user can try again", async () => {
    await signInWithBiometricUnlock();
    keychain.promptResult = "cancel";
    await expect(store().unlock()).rejects.toThrow("canceled");
    expect(store().status).toBe("locked");
    expect(stored(PROTECTED)).toBe(true);

    keychain.promptResult = "match";
    await store().unlock();
    expect(store().status).toBe("signedIn");
  });

  it("an enrollment change signs out with a notice but keeps queued edits", async () => {
    await signInWithBiometricUnlock();
    queueEdit("spot-1", student.id);
    keychain.enrollmentChanged = true;

    await store().unlock();

    expect(store()).toMatchObject({ status: "signedOut", token: null, notice: BIOMETRICS_CHANGED_NOTICE, biometricEnabled: false });
    expect([stored(PLAIN), stored(FLAG), stored(PROTECTED)]).toEqual([false, false, false]);
    expect(queuedSpotIds()).toEqual(["spot-1"]);
  });

  it("an unlocked token that has expired signs out with the expiry notice", async () => {
    await signIn(student, tokenExpiringIn(-1_000));
    await store().setBiometric(true);
    useSessionStore.setState({ status: "locked", token: null, user: null });
    await store().unlock();
    expect(store()).toMatchObject({ status: "signedOut", notice: EXPIRED_NOTICE });
  });

  it("turning it off writes the plain item back without a prompt", async () => {
    await signInWithBiometricUnlock();
    await store().unlock();
    keychain.prompts = 0;

    await store().setBiometric(false);

    expect(store().biometricEnabled).toBe(false);
    expect(keychain.prompts).toBe(0);
    expect([stored(PLAIN), stored(FLAG), stored(PROTECTED)]).toEqual([true, false, false]);
    await coldStart();
    expect(store().status).toBe("signedIn");
  });
});

describe("sign out vs. an ended session", () => {
  it("sign out discards every queued edit and its optimistic row, and deletes both stored copies", async () => {
    await signIn();
    await store().setBiometric(true);
    queueEdit("spot-1", student.id);
    queueEdit("spot-2", null);

    await store().logout();

    expect(store()).toMatchObject({ status: "signedOut", token: null, user: null, notice: null });
    expect(outboxDao.count()).toBe(0);
    expect(getSpotById("spot-1")).toBeNull();
    expect(getSpotById("spot-2")).toBeNull();
    expect(queryClient.invalidateQueries).toHaveBeenCalled();
    expect([stored(PLAIN), stored(FLAG), stored(PROTECTED)]).toEqual([false, false, false]);
  });

  it("a 401 (expire) keeps queued edits and pending rows for the same user", async () => {
    await signIn();
    queueEdit("spot-1", student.id);

    await store().expire();

    expect(store()).toMatchObject({ status: "signedOut", token: null, notice: EXPIRED_NOTICE });
    expect(stored(PLAIN)).toBe(false);
    expect(queuedSpotIds()).toEqual(["spot-1"]);
    expect(getSpotById("spot-1")?.pendingSync).toBe(true);

    await signIn(student);
    expect(queuedSpotIds()).toEqual(["spot-1"]);
  });

  it("expire does nothing unless signed in (a late 401 after sign-out keeps the login notice clear)", async () => {
    await coldStart();
    await store().expire();
    expect(store().notice).toBeNull();
  });

  it("signing in as someone else discards the previous user's queued edits, not ownerless ones", async () => {
    await signIn(student);
    queueEdit("spot-1", student.id);
    queueEdit("spot-2", null);
    await store().expire();

    await signIn(otherStudent);

    expect(store().user).toEqual(otherStudent);
    expect(queuedSpotIds()).toEqual(["spot-2"]);
    expect(getSpotById("spot-1")).toBeNull();
    expect(getSpotById("spot-2")?.pendingSync).toBe(true);
    expect(queryClient.invalidateQueries).toHaveBeenCalled();
  });
});
