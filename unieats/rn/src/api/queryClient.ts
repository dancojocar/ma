import { focusManager, QueryClient } from "@tanstack/react-query";
import { AppState } from "react-native";
import { isClientError } from "./errors";

const MAX_RETRIES = 2;

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (failureCount, error) => failureCount < MAX_RETRIES && !isClientError(error),
    },
  },
});

AppState.addEventListener("change", (status) => focusManager.setFocused(status === "active"));
