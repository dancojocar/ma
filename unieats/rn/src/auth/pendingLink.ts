import type { Href } from "expo-router";

const SPOT_LINK = /^(?:unieats:\/\/\/?|https:\/\/unieats\.app\/|\/)?spots\/([^/?#]+)\/?(?:[?#].*)?$/;

let pendingSpotId: string | null = null;

export function spotIdFromLink(url: string): string | null {
  const match = SPOT_LINK.exec(url);
  if (!match) return null;
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return null;
  }
}

/** Keeps a spot link that arrived before the user was signed in, so sign-in or unlock can open it. */
export function rememberPendingLink(url: string) {
  const id = spotIdFromLink(url);
  if (id) pendingSpotId = id;
}

export function takePendingLink(): Href | null {
  const id = pendingSpotId;
  pendingSpotId = null;
  return id ? { pathname: "/spots/[id]", params: { id } } : null;
}

export function forgetPendingLink() {
  pendingSpotId = null;
}
