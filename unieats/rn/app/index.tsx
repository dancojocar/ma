import { Redirect, type Href } from "expo-router";
import { useState } from "react";
import { takePendingLink } from "../src/auth/pendingLink";
import { useSessionStore } from "../src/store/sessionStore";

export default function Index() {
  const status = useSessionStore((s) => s.status);
  if (status === "restoring") return null;
  if (status === "locked") return <Redirect href="/unlock" />;
  if (status === "signedOut") return <Redirect href="/login" />;
  return <SignedInRedirect />;
}

/** Opens the deep link that arrived before sign-in or unlock, if any. */
function SignedInRedirect() {
  const [href] = useState<Href>(() => takePendingLink() ?? "/spots");
  return <Redirect href={href} />;
}
