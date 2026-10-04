import { Redirect } from "expo-router";
import { LoadingView } from "../src/components/StatusViews";
import { useSessionStore } from "../src/store/sessionStore";

export default function Index() {
  const status = useSessionStore((s) => s.status);
  if (status === "restoring") return <LoadingView />;
  return <Redirect href={status === "signedIn" ? "/spots" : "/login"} />;
}
