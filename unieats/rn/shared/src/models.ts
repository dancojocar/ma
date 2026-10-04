export type SpotCategory = "cafe" | "canteen" | "fastfood" | "bakery" | "bar";

export interface Spot {
  id: string;
  name: string;
  category: SpotCategory;
  rating: number;
  priceLevel: number;
  lat: number;
  lng: number;
  openNow: boolean;
  photoUrl: string;
  description: string;
  updatedAt: number;
}

export interface Review {
  id: string;
  spotId: string;
  author: string;
  stars: number;
  text: string;
  createdAt: number;
}

export interface User {
  id: string;
  email: string;
  displayName: string;
}

export interface LocalSpot extends Spot {
  pendingSync: boolean;
}

export type SpotEdit = Pick<Spot, "name" | "description" | "openNow">;

export interface OutboxOp {
  seq: number;
  opId: string;
  type: "update";
  entityId: string;
  /** `updatedAt` is the server version the edit was based on (CONTRACT §3). */
  payload: SpotEdit & { updatedAt: number };
  createdAt: number;
}
