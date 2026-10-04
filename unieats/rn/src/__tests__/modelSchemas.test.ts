import {
  LiveEventSchema,
  LoginResponseSchema,
  RemoteConfigSchema,
  ReviewSchema,
  SpotPageSchema,
  SpotSchema,
} from "@unieats/shared";

const pizzaStop = {
  id: "spot-3",
  name: "Pizza Stop",
  category: "fastfood",
  rating: 4.1,
  priceLevel: 2,
  lat: 44.426,
  lng: 26.1015,
  openNow: true,
  photoUrl: "https://picsum.photos/seed/spot-3/400/300",
  description: "Pizza by the slice for students on the go.",
  updatedAt: 1700000002000,
};

describe("SpotSchema", () => {
  it("parses a canonical seed spot", () => {
    expect(SpotSchema.parse(pizzaStop)).toEqual(pizzaStop);
  });

  it("strips local-only fields such as pendingSync", () => {
    expect(SpotSchema.parse({ ...pizzaStop, pendingSync: true })).not.toHaveProperty("pendingSync");
  });

  it("rejects a category outside the contract", () => {
    expect(SpotSchema.safeParse({ ...pizzaStop, category: "sushi" }).success).toBe(false);
  });

  it("rejects a missing name", () => {
    const { name: _name, ...withoutName } = pizzaStop;
    expect(SpotSchema.safeParse(withoutName).success).toBe(false);
  });

  it("rejects the old field name imageUrl in place of photoUrl", () => {
    const { photoUrl: _photo, ...rest } = pizzaStop;
    expect(SpotSchema.safeParse({ ...rest, imageUrl: pizzaStop.photoUrl }).success).toBe(false);
  });
});

describe("SpotPageSchema", () => {
  it("parses a page with hasNextPage", () => {
    const page = SpotPageSchema.parse({ spots: [pizzaStop], page: 1, hasNextPage: true });
    expect(page.hasNextPage).toBe(true);
    expect(page.spots).toHaveLength(1);
  });
});

describe("ReviewSchema", () => {
  const review = {
    id: "review-1",
    spotId: "spot-1",
    author: "Demo Student",
    stars: 4,
    text: "Good value for money.",
    createdAt: 1700000100000,
  };

  it("parses a valid review", () => {
    expect(ReviewSchema.safeParse(review).success).toBe(true);
  });

  it("rejects stars outside 1..5", () => {
    expect(ReviewSchema.safeParse({ ...review, stars: 0 }).success).toBe(false);
    expect(ReviewSchema.safeParse({ ...review, stars: 6 }).success).toBe(false);
  });
});

describe("LoginResponseSchema", () => {
  it("parses { token, user }", () => {
    const payload = {
      token: "header.payload.signature",
      user: { id: "user-1", email: "student@unieats.app", displayName: "Demo Student" },
    };
    expect(LoginResponseSchema.safeParse(payload).success).toBe(true);
  });
});

describe("LiveEventSchema", () => {
  it("parses spot.updated with a spot", () => {
    const event = LiveEventSchema.parse({ type: "spot.updated", spot: pizzaStop });
    expect(event.type).toBe("spot.updated");
  });

  it("parses spot.deleted with an id", () => {
    expect(LiveEventSchema.parse({ type: "spot.deleted", id: "spot-3" })).toEqual({
      type: "spot.deleted",
      id: "spot-3",
    });
  });

  it("rejects an unknown event type", () => {
    expect(LiveEventSchema.safeParse({ type: "spot.renamed", id: "spot-3" }).success).toBe(false);
  });
});

describe("RemoteConfigSchema", () => {
  it("parses the show_new_rating_ui flag", () => {
    expect(RemoteConfigSchema.parse({ flags: { show_new_rating_ui: true } }).flags.show_new_rating_ui).toBe(true);
  });
});
