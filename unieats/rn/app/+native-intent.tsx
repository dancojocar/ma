import { rememberPendingLink } from "../src/auth/pendingLink";
import { useSessionStore } from "../src/store/sessionStore";

/** Called for the launch URL and every incoming link; a throw here would crash the app. */
export function redirectSystemPath({ path }: { path: string; initial: boolean }) {
  try {
    if (useSessionStore.getState().status !== "signedIn") rememberPendingLink(path);
  } catch {}
  return path;
}
