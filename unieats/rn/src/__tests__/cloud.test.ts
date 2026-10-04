import { getRemoteConfig } from "../api/client";
import { createCrashReporter, installGlobalErrorHandler, loadCrashConsent, useCrashConsent } from "../cloud/crashReporting";
import { DEFAULT_FLAGS, useFeatureFlags } from "../cloud/featureFlags";
import { getDb } from "../db/database";
import { readSetting } from "../db/settingsDao";

jest.mock("expo-sqlite", () => jest.requireActual("./support/memorySqlite"));
jest.mock("../api/client", () => ({ getRemoteConfig: jest.fn() }));

beforeEach(() => {
  getDb().execSync("DELETE FROM settings");
  useFeatureFlags.setState(useFeatureFlags.getInitialState(), true);
  useCrashConsent.setState(useCrashConsent.getInitialState(), true);
});

describe("remote config", () => {
  it("uses the shipped defaults until Fetch & activate", () => {
    expect(useFeatureFlags.getState()).toMatchObject({ flags: DEFAULT_FLAGS, lastFetchedAt: null });
    expect(DEFAULT_FLAGS.show_new_rating_ui).toBe(false);
  });

  it("Fetch & activate applies the server's flags and records when", async () => {
    jest.mocked(getRemoteConfig).mockResolvedValue({ flags: { show_new_rating_ui: true } });
    const before = Date.now();
    await useFeatureFlags.getState().fetchAndActivate();
    expect(useFeatureFlags.getState().flags.show_new_rating_ui).toBe(true);
    expect(useFeatureFlags.getState().lastFetchedAt).toBeGreaterThanOrEqual(before);
  });

  it("keeps the active flags when the fetch fails", async () => {
    jest.mocked(getRemoteConfig).mockRejectedValue(new Error("offline"));
    await expect(useFeatureFlags.getState().fetchAndActivate()).rejects.toThrow("offline");
    expect(useFeatureFlags.getState()).toMatchObject({ flags: DEFAULT_FLAGS, lastFetchedAt: null });
  });
});

describe("crash reporting consent", () => {
  const backend = { recordError: jest.fn() };
  const reporter = createCrashReporter(backend);

  beforeEach(() => backend.recordError.mockClear());

  it("is off by default and nothing reaches the backend", () => {
    loadCrashConsent();
    expect(useCrashConsent.getState().consent).toBe(false);
    reporter.recordError(new Error("not sent"));
    expect(backend.recordError).not.toHaveBeenCalled();
  });

  it("once on, errors reach the backend with their context", () => {
    useCrashConsent.getState().setConsent(true);
    const error = new Error("sent");
    reporter.recordError(error, { boundary: "spots" });
    expect(backend.recordError).toHaveBeenCalledWith(error, { boundary: "spots" });
  });

  it("is stored in the settings table and survives a restart", () => {
    useCrashConsent.getState().setConsent(true);
    expect(readSetting("crashReportingConsent")).toBe("true");
    useCrashConsent.setState({ consent: false });
    loadCrashConsent();
    expect(useCrashConsent.getState().consent).toBe(true);

    useCrashConsent.getState().setConsent(false);
    useCrashConsent.setState({ consent: true });
    loadCrashConsent();
    expect(useCrashConsent.getState().consent).toBe(false);
  });

  it("the global handler reports only with consent and always calls React Native's previous handler", () => {
    const previous = jest.fn();
    let installed: ((error: unknown, isFatal?: boolean) => void) | undefined;
    const setGlobalHandler = jest.fn((handler: (error: unknown, isFatal?: boolean) => void) => {
      installed = handler;
    });
    Object.assign(globalThis, { ErrorUtils: { getGlobalHandler: () => previous, setGlobalHandler } });
    const logged = jest.spyOn(console, "error").mockImplementation(() => {});

    installGlobalErrorHandler();
    installGlobalErrorHandler();
    expect(setGlobalHandler).toHaveBeenCalledTimes(1);

    installed!(new Error("without consent"), true);
    expect(logged).not.toHaveBeenCalled();
    expect(previous).toHaveBeenLastCalledWith(new Error("without consent"), true);

    useCrashConsent.getState().setConsent(true);
    installed!("a thrown string", false);
    expect(logged).toHaveBeenCalledWith("[CrashReporter] Error: a thrown string", { isFatal: false });
    expect(previous).toHaveBeenCalledTimes(2);
  });
});
