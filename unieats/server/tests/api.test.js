const request = require('supertest');
const app = require('../src/app');

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
  it('returns the canonical seed with fixed ids spot-1..spot-8 on the first page', async () => {
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
