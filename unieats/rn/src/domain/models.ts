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
