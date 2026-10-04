import type { Spot, SpotEdit } from "@unieats/shared";
import { ApiError } from "../../api/errors";

type Patch = SpotEdit & { updatedAt: number };

export interface SentPatch {
  id: string;
  body: Patch;
  token: string;
  idempotencyKey: string;
}

/**
 * The server's PATCH / GET /spots/:id rules in memory: 409 with `{ spot }` when the edit's base version is
 * older than the stored copy, 400 for an empty name, a new `updatedAt` on every accepted write. The hooks run
 * while a request is "in flight", which is where the edits and sign-outs under test happen.
 */
export class FakeSpotServer {
  spots = new Map<string, Spot>();
  patches: SentPatch[] = [];
  gets = 0;
  status: number | null = null;
  onPatch: ((patch: SentPatch) => void) | null = null;
  onGet: ((id: string) => void) | null = null;
  private clock = 10_000;

  put(spot: Spot) {
    this.spots.set(spot.id, { ...spot });
  }

  patchSpot = async (id: string, body: Patch, auth: { token: string; idempotencyKey: string }): Promise<Spot> => {
    const patch = { id, body: { ...body }, ...auth };
    this.patches.push(patch);
    await Promise.resolve();
    this.onPatch?.(patch);
    if (this.status) throw new ApiError("http", `PATCH → ${this.status}`, this.status);
    const current = this.spots.get(id);
    if (!current) throw new ApiError("http", "PATCH → 404", 404, "not_found");
    if (body.updatedAt < current.updatedAt) {
      throw new ApiError("http", "PATCH → 409", 409, "conflict", { error: { code: "conflict" }, spot: current });
    }
    if (!body.name) throw new ApiError("http", "PATCH → 400", 400, "validation");
    const next = { ...current, name: body.name, description: body.description, openNow: body.openNow, updatedAt: ++this.clock };
    this.spots.set(id, next);
    return { ...next };
  };

  getSpot = async (id: string): Promise<Spot> => {
    this.gets += 1;
    await Promise.resolve();
    this.onGet?.(id);
    const current = this.spots.get(id);
    if (!current) throw new ApiError("http", "GET → 404", 404, "not_found");
    return { ...current };
  };
}
