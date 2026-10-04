import { z } from "zod";
import type { Review, Spot } from "./models";

export const SpotCategorySchema = z.enum(["cafe", "canteen", "fastfood", "bakery", "bar"]);

export const SpotSchema: z.ZodType<Spot> = z.object({
  id: z.string(),
  name: z.string(),
  category: SpotCategorySchema,
  rating: z.number(),
  priceLevel: z.number().int(),
  lat: z.number(),
  lng: z.number(),
  openNow: z.boolean(),
  photoUrl: z.string(),
  description: z.string(),
  updatedAt: z.number().int(),
});

export const ReviewSchema: z.ZodType<Review> = z.object({
  id: z.string(),
  spotId: z.string(),
  author: z.string(),
  stars: z.number().int().min(1).max(5),
  text: z.string(),
  createdAt: z.number().int(),
});

export const SpotPageSchema = z.object({
  spots: z.array(SpotSchema),
  page: z.number().int(),
  hasNextPage: z.boolean(),
});

export type SpotPage = z.infer<typeof SpotPageSchema>;
