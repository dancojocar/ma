import { create } from "zustand";
import { readSetting, writeSetting } from "../db/settingsDao";

export interface CrashReporter {
  recordError(error: Error, context?: Record<string, unknown>): void;
}

/** Default backend: logs. A Sentry/Crashlytics implementation would plug in here without touching callers. */
export const logCrashReporter: CrashReporter = {
  recordError(error, context) {
    console.error(`[CrashReporter] ${error.name}: ${error.message}`, context ?? {});
  },
};

const CONSENT_KEY = "crashReportingConsent";

interface ConsentState {
  consent: boolean;
  setConsent: (consent: boolean) => void;
}

export const useCrashConsent = create<ConsentState>()((set) => ({
  consent: false,
  setConsent: (consent) => {
    writeSetting(CONSENT_KEY, consent ? "true" : "false");
    set({ consent });
  },
}));

export function loadCrashConsent() {
  useCrashConsent.setState({ consent: readSetting(CONSENT_KEY) === "true" });
}

/** Opt-in gate: nothing reaches the backend until the user has turned consent on. */
export function createCrashReporter(backend: CrashReporter): CrashReporter {
  return {
    recordError(error, context) {
      if (useCrashConsent.getState().consent) backend.recordError(error, context);
    },
  };
}

export const crashReporter = createCrashReporter(logCrashReporter);

let handlerInstalled = false;

/** Routes uncaught JS errors to the reporter, then to React Native's default handler (red box / crash). */
export function installGlobalErrorHandler() {
  if (handlerInstalled) return;
  handlerInstalled = true;
  const previous = ErrorUtils.getGlobalHandler();
  ErrorUtils.setGlobalHandler((error, isFatal) => {
    crashReporter.recordError(error instanceof Error ? error : new Error(String(error)), { isFatal });
    previous(error, isFatal);
  });
}
