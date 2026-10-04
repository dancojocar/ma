import { QueryClient } from "@tanstack/react-query";
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
