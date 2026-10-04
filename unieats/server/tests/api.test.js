const request = require('supertest');
const app = require('../src/app');
const { withAuth } = require('./helpers');

const api = withAuth(request(app));

const SPOT_FIELDS = {
  id: 'string',
  name: 'string',
  rating: 'number',
  lat: 'number',
  lng: 'number',
  openNow: 'boolean',
  photoUrl: 'string',
  description: 'string',
  updatedAt: 'number',
};

function expectSpotShape(spot) {
  for (const [field, type] of Object.entries(SPOT_FIELDS)) {
    expect(typeof spot[field]).toBe(type);
  }
  expect(['cafe', 'canteen', 'fastfood', 'bakery', 'bar']).toContain(spot.category);
  expect([1, 2, 3]).toContain(spot.priceLevel);
}

describe('GET /api/health', () => {
  it('returns { ok: true }', async () => {
    const res = await request(app).get('/api/health');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ ok: true });
  });
});

describe('GET /api/spots', () => {
  it('starts with the canonical seed (fixed ids spot-1..spot-8)', async () => {
    const res = await request(app).get('/api/spots');
    expect(res.status).toBe(200);
    expect(res.body.page).toBe(1);
    expect(res.body.spots.slice(0, 8).map((s) => s.id)).toEqual(
      ['spot-1', 'spot-2', 'spot-3', 'spot-4', 'spot-5', 'spot-6', 'spot-7', 'spot-8']
    );
    res.body.spots.forEach(expectSpotShape);
  });

  it('serves real photo URLs', async () => {
    const res = await request(app).get('/api/spots/spot-1');
    expect(res.body.photoUrl).toBe('https://picsum.photos/seed/spot-1/400/300');
  });

  it('serves 25 spots: page size 20 gives a second page of 5', async () => {
    const first = await request(app).get('/api/spots');
    expect(first.body.spots).toHaveLength(20);
    expect(first.body.hasNextPage).toBe(true);
    const second = await request(app).get('/api/spots?page=2');
    expect(second.body.spots).toHaveLength(5);
    expect(second.body.spots[4].id).toBe('spot-25');
    expect(second.body.hasNextPage).toBe(false);
  });

  it('mixes open and closed spots', async () => {
    const res = await request(app).get('/api/spots');
    const open = res.body.spots.map((s) => s.openNow);
    expect(open).toContain(true);
    expect(open).toContain(false);
  });

  it('paginates with page + limit and reports hasNextPage', async () => {
    const res = await request(app).get('/api/spots?page=1&limit=3');
    expect(res.status).toBe(200);
    expect(res.body.spots).toHaveLength(3);
    expect(res.body.page).toBe(1);
    expect(res.body.hasNextPage).toBe(true);
  });

  it('second page continues where the first stopped', async () => {
    const first = await request(app).get('/api/spots?page=1&limit=3');
    const second = await request(app).get('/api/spots?page=2&limit=3');
    expect(second.body.spots[0].id).not.toBe(first.body.spots[2].id);
    expect(second.body.page).toBe(2);
  });

  it('last page has hasNextPage=false', async () => {
    const res = await request(app).get('/api/spots?page=1&limit=100');
    expect(res.body.hasNextPage).toBe(false);
  });

  it('beyond the last page returns an empty array and hasNextPage=false', async () => {
    const res = await request(app).get('/api/spots?page=999&limit=20');
    expect(res.status).toBe(200);
    expect(res.body.spots).toEqual([]);
    expect(res.body.hasNextPage).toBe(false);
  });

  it('falls back to page 1 / limit 20 on invalid query values', async () => {
    const res = await request(app).get('/api/spots?page=abc&limit=-5');
    expect(res.status).toBe(200);
    expect(res.body.page).toBe(1);
    expect(res.body.spots.length).toBeLessThanOrEqual(20);
  });

  it('?q= searches name case-insensitively ("pizza" finds Pizza Stop)', async () => {
    const res = await request(app).get('/api/spots?q=PiZzA');
    expect(res.status).toBe(200);
    expect(res.body.spots.map((s) => s.name)).toContain('Pizza Stop');
  });

  it('?q= also searches the description', async () => {
    const res = await request(app).get('/api/spots?q=library');
    expect(res.body.spots.map((s) => s.id)).toEqual(['spot-2']);
  });

  it('?q= with no match returns an empty page', async () => {
    const res = await request(app).get('/api/spots?q=zzzz-no-such-spot');
    expect(res.body.spots).toEqual([]);
    expect(res.body.hasNextPage).toBe(false);
  });

  it('?category= returns only that category', async () => {
    const res = await request(app).get('/api/spots?category=cafe');
    expect(res.body.spots.length).toBeGreaterThan(0);
    res.body.spots.forEach((s) => expect(s.category).toBe('cafe'));
  });

  it('?q= and ?category= combine', async () => {
    const res = await request(app).get('/api/spots?q=campus&category=cafe');
    expect(res.body.spots.map((s) => s.id)).toContain('spot-7');
    res.body.spots.forEach((s) => expect(s.category).toBe('cafe'));
  });
});

describe('GET /api/spots/:id', () => {
  it('returns the full spot', async () => {
    const res = await request(app).get('/api/spots/spot-3');
    expect(res.status).toBe(200);
    expectSpotShape(res.body);
    expect(res.body).toMatchObject({ id: 'spot-3', name: 'Pizza Stop', category: 'fastfood' });
  });

  it('404 with the error envelope for an unknown id', async () => {
    const res = await request(app).get('/api/spots/does-not-exist');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
    expect(typeof res.body.error.message).toBe('string');
  });
});

describe('Unknown routes', () => {
  it('404 with the error envelope', async () => {
    const res = await request(app).get('/api/nope');
    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: { code: 'not_found', message: 'Route not found' } });
  });
});

describe('Chaos headers', () => {
  it('X-Chaos-Delay delays the response', async () => {
    const start = Date.now();
    const res = await request(app).get('/api/health').set('X-Chaos-Delay', '200');
    expect(res.status).toBe(200);
    expect(Date.now() - start).toBeGreaterThanOrEqual(180);
  });

  it('X-Chaos-Status overrides the status code', async () => {
    const res = await request(app).get('/api/spots').set('X-Chaos-Status', '503');
    expect(res.status).toBe(503);
  });

  it('X-Chaos-Malformed returns truncated JSON', async () => {
    await expect(
      request(app).get('/api/spots').set('X-Chaos-Malformed', '1')
    ).rejects.toThrow(/JSON|parse|token/i);
  });

  it('X-Chaos-Drop closes the socket', async () => {
    await expect(request(app).get('/api/spots').set('X-Chaos-Drop', '1')).rejects.toThrow();
  });
});

const NEW_SPOT = {
  name: 'Test Spot',
  category: 'cafe',
  rating: 4.0,
  priceLevel: 2,
  lat: 44.43,
  lng: 26.1,
  openNow: true,
  photoUrl: '',
  description: 'A test spot',
};

function createSpot(overrides = {}) {
  return api.post('/api/spots').send({ ...NEW_SPOT, ...overrides });
}

describe('POST /api/spots', () => {
  it('201 with the full spot, a server-assigned id and updatedAt', async () => {
    const before = Date.now();
    const res = await createSpot();
    expect(res.status).toBe(201);
    expectSpotShape(res.body);
    expect(res.body).toMatchObject(NEW_SPOT);
    expect(res.body.id).toMatch(/^[0-9a-f-]{36}$/);
    expect(res.body.updatedAt).toBeGreaterThanOrEqual(before);

    const fetched = await request(app).get(`/api/spots/${res.body.id}`);
    expect(fetched.body).toEqual(res.body);
  });

  it('fills defaults for optional fields', async () => {
    const res = await api.post('/api/spots').send({ name: 'Minimal', category: 'bar' });
    expect(res.status).toBe(201);
    expect(res.body).toMatchObject({ rating: 0, priceLevel: 1, openNow: false, photoUrl: '', description: '' });
  });

  it('ignores a client-supplied id and unknown fields', async () => {
    const res = await createSpot({ id: 'spot-1', secret: 'x' });
    expect(res.status).toBe(201);
    expect(res.body.id).not.toBe('spot-1');
    expect(res.body.secret).toBeUndefined();
  });

  it.each([
    ['name missing', { name: undefined }],
    ['category missing', { category: undefined }],
    ['unknown category', { category: 'restaurant' }],
    ['rating out of range', { rating: 7 }],
    ['priceLevel out of range', { priceLevel: 4 }],
    ['openNow not boolean', { openNow: 'yes' }],
  ])('400 validation when %s', async (_, overrides) => {
    const res = await createSpot(overrides);
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
    expect(typeof res.body.error.message).toBe('string');
  });

  it('400 validation on a malformed JSON body', async () => {
    const res = await api
      .post('/api/spots')
      .set('Content-Type', 'application/json')
      .send('{"name": "broken"');
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});

describe('PATCH /api/spots/:id', () => {
  let id;

  beforeAll(async () => {
    id = (await createSpot({ name: 'Patchable', openNow: false })).body.id;
  });

  it('applies the partial update and bumps updatedAt', async () => {
    const before = (await request(app).get(`/api/spots/${id}`)).body;
    const res = await api.patch(`/api/spots/${id}`).send({ openNow: true, name: 'Patched' });
    expect(res.status).toBe(200);
    expect(res.body).toMatchObject({ id, openNow: true, name: 'Patched', category: before.category });
    expect(res.body.updatedAt).toBeGreaterThanOrEqual(before.updatedAt);
  });

  it('cannot change the id', async () => {
    const res = await api.patch(`/api/spots/${id}`).send({ id: 'hijack' });
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(id);
  });

  it('400 validation on an invalid field', async () => {
    const res = await api.patch(`/api/spots/${id}`).send({ category: 'restaurant' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('404 for an unknown spot', async () => {
    const res = await api.patch('/api/spots/ghost').send({ name: 'ghost' });
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
  });
});

describe('DELETE /api/spots/:id', () => {
  it('204, then the spot and its reviews are gone', async () => {
    const { id } = (await createSpot({ name: 'Deletable' })).body;
    await api.post(`/api/spots/${id}/reviews`).send({ stars: 3 });

    const res = await api.delete(`/api/spots/${id}`);
    expect(res.status).toBe(204);
    expect(res.text).toBe('');
    expect((await request(app).get(`/api/spots/${id}`)).status).toBe(404);
    expect((await request(app).get(`/api/spots/${id}/reviews`)).status).toBe(404);
  });

  it('404 for an unknown spot', async () => {
    const res = await api.delete('/api/spots/ghost');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
  });
});

describe('GET /api/spots/:id/reviews', () => {
  it('returns the seeded reviews with the Review shape', async () => {
    const res = await request(app).get('/api/spots/spot-1/reviews');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(2);
    res.body.forEach((r) => {
      expect(r.spotId).toBe('spot-1');
      expect(typeof r.id).toBe('string');
      expect(typeof r.author).toBe('string');
      expect(Number.isInteger(r.stars)).toBe(true);
      expect(typeof r.text).toBe('string');
      expect(typeof r.createdAt).toBe('number');
    });
  });

  it('returns an empty array for a spot without reviews', async () => {
    const res = await request(app).get('/api/spots/spot-25/reviews');
    expect(res.status).toBe(200);
    expect(res.body).toEqual([]);
  });

  it('404 for an unknown spot', async () => {
    const res = await request(app).get('/api/spots/ghost/reviews');
    expect(res.status).toBe(404);
  });
});

describe('POST /api/spots/:id/reviews', () => {
  it('201 with the new review, which then appears in the list', async () => {
    const res = await api
      .post('/api/spots/spot-4/reviews')
      .send({ stars: 5, text: 'Warm bread at 8 am' });
    expect(res.status).toBe(201);
    expect(res.body).toMatchObject({ spotId: 'spot-4', stars: 5, text: 'Warm bread at 8 am' });
    expect(typeof res.body.author).toBe('string');
    expect(typeof res.body.createdAt).toBe('number');

    const list = await request(app).get('/api/spots/spot-4/reviews');
    expect(list.body.map((r) => r.id)).toContain(res.body.id);
  });

  it.each([0, 6, 3.5, '4', undefined])('400 validation for stars=%p', async (stars) => {
    const res = await api.post('/api/spots/spot-4/reviews').send({ stars, text: 'x' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('404 for an unknown spot', async () => {
    const res = await api.post('/api/spots/ghost/reviews').send({ stars: 3 });
    expect(res.status).toBe(404);
  });
});

describe('Idempotency-Key', () => {
  const key = () => `idem-${Math.random().toString(36).slice(2)}`;

  it('POST /spots replays the original response and creates only one spot', async () => {
    const k = key();
    const total = async () => (await request(app).get('/api/spots?limit=1000')).body.spots.length;
    const countBefore = await total();

    const first = await createSpot({ name: 'Once' }).set('Idempotency-Key', k);
    const second = await createSpot({ name: 'Ignored' }).set('Idempotency-Key', k);
    expect(first.status).toBe(201);
    expect(second.status).toBe(201);
    expect(second.body).toEqual(first.body);
    expect(second.headers['idempotent-replayed']).toBe('true');
    expect(await total()).toBe(countBefore + 1);
  });

  it('PATCH replays the original response', async () => {
    const k = key();
    const first = await api.patch('/api/spots/spot-7').set('Idempotency-Key', k).send({ description: 'first' });
    const second = await api.patch('/api/spots/spot-7').set('Idempotency-Key', k).send({ description: 'second' });
    expect(second.status).toBe(200);
    expect(second.body.description).toBe('first');
    expect((await request(app).get('/api/spots/spot-7')).body.description).toBe('first');
    expect(first.body.updatedAt).toBe(second.body.updatedAt);
  });

  it('DELETE replays 204 instead of 404', async () => {
    const k = key();
    const { id } = (await createSpot()).body;
    expect((await api.delete(`/api/spots/${id}`).set('Idempotency-Key', k)).status).toBe(204);
    expect((await api.delete(`/api/spots/${id}`).set('Idempotency-Key', k)).status).toBe(204);
  });

  it('review POST replays without creating a duplicate', async () => {
    const k = key();
    const first = await api.post('/api/spots/spot-5/reviews').set('Idempotency-Key', k).send({ stars: 4 });
    const second = await api.post('/api/spots/spot-5/reviews').set('Idempotency-Key', k).send({ stars: 4 });
    expect(second.body.id).toBe(first.body.id);
    const list = await request(app).get('/api/spots/spot-5/reviews');
    expect(list.body.filter((r) => r.id === first.body.id)).toHaveLength(1);
  });

  it('different keys produce independent results', async () => {
    const a = await createSpot().set('Idempotency-Key', key());
    const b = await createSpot().set('Idempotency-Key', key());
    expect(a.body.id).not.toBe(b.body.id);
  });

  it('a request without the header is not idempotent', async () => {
    const a = await createSpot();
    const b = await createSpot();
    expect(a.body.id).not.toBe(b.body.id);
  });
});

describe('Chaos headers on mutations and reviews', () => {
  it('X-Chaos-Status applies to POST /spots', async () => {
    const res = await createSpot().set('X-Chaos-Status', '500');
    expect(res.status).toBe(500);
  });

  it('X-Chaos-Delay applies to GET reviews', async () => {
    const start = Date.now();
    const res = await request(app).get('/api/spots/spot-1/reviews').set('X-Chaos-Delay', '150');
    expect(res.status).toBe(200);
    expect(Date.now() - start).toBeGreaterThanOrEqual(130);
  });

  it('X-Chaos-Drop applies to PATCH', async () => {
    await expect(
      api.patch('/api/spots/spot-1').set('X-Chaos-Drop', '1').send({ name: 'x' })
    ).rejects.toThrow();
  });
});

describe('PATCH conflict rule (updatedAt)', () => {
  let spot;

  beforeEach(async () => {
    spot = (await createSpot({ name: 'Contested', openNow: false })).body;
  });

  it('applies a write based on the current version and returns a newer updatedAt', async () => {
    const res = await api
      .patch(`/api/spots/${spot.id}`)
      .send({ openNow: true, updatedAt: spot.updatedAt });
    expect(res.status).toBe(200);
    expect(res.body.openNow).toBe(true);
    expect(res.body.updatedAt).toBeGreaterThan(spot.updatedAt);
  });

  it('applies a write whose updatedAt is newer than the server copy', async () => {
    const res = await api
      .patch(`/api/spots/${spot.id}`)
      .send({ name: 'Mine', updatedAt: spot.updatedAt + 60_000 });
    expect(res.status).toBe(200);
    expect(res.body.name).toBe('Mine');
  });

  it('409 conflict with the server copy when the write is based on a stale version', async () => {
    const fresh = await api.patch(`/api/spots/${spot.id}`).send({ name: 'Theirs' });

    const res = await api
      .patch(`/api/spots/${spot.id}`)
      .send({ name: 'Mine', updatedAt: spot.updatedAt });
    expect(res.status).toBe(409);
    expect(res.body.error.code).toBe('conflict');
    expect(typeof res.body.error.message).toBe('string');
    expect(res.body.spot).toEqual(fresh.body);

    const stored = await request(app).get(`/api/spots/${spot.id}`);
    expect(stored.body.name).toBe('Theirs');
  });

  it('two writes from the same old version: the first wins, the second gets 409', async () => {
    const first = await api.patch(`/api/spots/${spot.id}`).send({ name: 'A', updatedAt: spot.updatedAt });
    const second = await api.patch(`/api/spots/${spot.id}`).send({ name: 'B', updatedAt: spot.updatedAt });
    expect(first.status).toBe(200);
    expect(second.status).toBe(409);
    expect(second.body.spot.name).toBe('A');
  });

  it('a PATCH without updatedAt is applied unconditionally', async () => {
    await api.patch(`/api/spots/${spot.id}`).send({ name: 'Theirs' });
    const res = await api.patch(`/api/spots/${spot.id}`).send({ name: 'Blind write' });
    expect(res.status).toBe(200);
  });

  it('400 validation when updatedAt is not a number', async () => {
    const res = await api.patch(`/api/spots/${spot.id}`).send({ name: 'x', updatedAt: 'yesterday' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('replaying a conflicted operation with the same Idempotency-Key returns the same 409', async () => {
    await api.patch(`/api/spots/${spot.id}`).send({ name: 'Theirs' });
    const send = () =>
      api
        .patch(`/api/spots/${spot.id}`)
        .set('Idempotency-Key', `conflict-${spot.id}`)
        .send({ name: 'Mine', updatedAt: spot.updatedAt });
    const first = await send();
    const second = await send();
    expect(first.status).toBe(409);
    expect(second.status).toBe(409);
    expect(second.body).toEqual(first.body);
  });
});
