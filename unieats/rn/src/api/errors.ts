import { API_URL } from "./config";

export type ApiErrorKind = "http" | "network" | "timeout" | "parse";

export class ApiError extends Error {
  constructor(
    readonly kind: ApiErrorKind,
    message: string,
    readonly status?: number,
    readonly code?: string,
    readonly body?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export function isClientError(error: unknown): boolean {
  return (
    error instanceof ApiError &&
    error.kind === "http" &&
    error.status !== undefined &&
    error.status >= 400 &&
    error.status < 500
  );
}

export function describeError(error: unknown): string {
  if (!(error instanceof ApiError)) return "Something went wrong.";
  switch (error.kind) {
    case "network":
      return `Can't reach the server at ${API_URL}.`;
    case "timeout":
      return "The server took too long to answer.";
    case "parse":
      return "The server sent an unexpected response.";
    case "http":
      return error.status === 404 ? "Not found." : `Server error (${error.status}).`;
  }
}
