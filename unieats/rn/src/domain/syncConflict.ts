export type ConflictWinner = "server" | "client";

/** Last-write-wins on `updatedAt`: the server copy wins only when it is strictly newer (a tie keeps the client edit). */
export function resolveConflict(localUpdatedAt: number, serverUpdatedAt: number): ConflictWinner {
  return serverUpdatedAt > localUpdatedAt ? "server" : "client";
}
