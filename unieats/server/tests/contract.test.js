/**
 * CONTRACT.md conformance suite: one block per contract section, black-box over HTTP/WS.
 * Any server that claims to implement the UniEats contract must pass it unchanged.
 */
const http = require('http');
const crypto = require('crypto');
const request = require('supertest');
const WebSocket = require('ws');
const app = require('../src/app');
const { attachLive } = require('../src/live');
const { parseEvents } = require('./sse');

const DEMO_EMAIL = 'student@unieats.app';
const DEMO_PASS = 'password';
const SEED_SPOT_1 = 'spot-1';
const SEED_SPOT_2 = 'spot-2';
const CATEGORIES = ['cafe', 'canteen', 'fastfood', 'bakery', 'bar'];
const VALID_SPOT = {
  name: 'Contract Test Spot',
  category: 'cafe',
  rating: 4.0,
  priceLevel: 2,
  lat: 44.43,
  lng: 26.1,
  openNow: true,
  photoUrl: '',
  description: 'contract test',
};

let token;
const bearer = () => `Bearer ${token}`;
const authed = {
  post: (url) => request(app).post(url).set('Authorization', bearer()),
  patch: (url) => request(app).patch(url).set('Authorization', bearer()),
  delete: (url) => request(app).delete(url).set('Authorization', bearer()),
};
const createSpot = (overrides = {}) => authed.post('/api/spots').send({ ...VALID_SPOT, ...overrides });
const b64 = (value) => Buffer.from(JSON.stringify(value)).toString('base64url');
const forge = (header, payload, secret = 'dev-secret-change-me') => {
  const unsigned = `${b64(header)}.${b64(payload)}`;
  return `${unsigned}.${crypto.createHmac('sha256', secret).update(unsigned).digest('base64url')}`;
};
const claims = (overrides = {}) => {
  const now = Math.floor(Date.now() / 1000);
  return { sub: 'user-1', email: DEMO_EMAIL, iss: 'unieats', aud: 'unieats-app', iat: now, exp: now + 3600, ...overrides };
};

beforeAll(async () => {
  const res = await request(app).post('/api/auth/login').send({ email: DEMO_EMAIL, password: DEMO_PASS });
  token = res.body.token;
});

describe('CONTRACT: GET /api/health', () => {
  it('200 { ok: true }', async () => {
    const res = await request(app).get('/api/health');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ ok: true });
  });
});

describe('CONTRACT: POST /api/auth/login', () => {
  it('200 with token + user { id, email, displayName }', async () => {
    const res = await request(app).post('/api/auth/login').send({ email: DEMO_EMAIL, password: DEMO_PASS });
    expect(res.status).toBe(200);
    expect(typeof res.body.token).toBe('string');
    expect(res.body.user).toEqual({ id: expect.any(String), email: DEMO_EMAIL, displayName: expect.any(String) });
  });

  it('token is an HS256 JWT with sub, email, iss=unieats, aud=unieats-app, exp = iat + 1h', async () => {
    const [header, payload] = token.split('.').slice(0, 2).map((p) => JSON.parse(Buffer.from(p, 'base64url').toString()));
    expect(header.alg).toBe('HS256');
    expect(payload).toMatchObject({ sub: expect.any(String), email: DEMO_EMAIL, iss: 'unieats', aud: 'unieats-app' });
    expect(payload.exp - payload.iat).toBe(3600);
    expect(payload.exp).toBeGreaterThan(Math.floor(Date.now() / 1000));
  });

  it('401 envelope on a wrong password', async () => {
    const res = await request(app).post('/api/auth/login').send({ email: DEMO_EMAIL, password: 'bad-pass' });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
    expect(typeof res.body.error.message).toBe('string');
  });

  it('400 validation when password is missing', async () => {
    const res = await request(app).post('/api/auth/login').send({ email: DEMO_EMAIL });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation when email is missing', async () => {
    const res = await request(app).post('/api/auth/login').send({ password: DEMO_PASS });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});

describe('CONTRACT: GET /api/spots (pagination + search)', () => {
  it('returns { spots, page, hasNextPage }', async () => {
    const res = await request(app).get('/api/spots');
    expect(res.status).toBe(200);
    expect(Array.isArray(res.body.spots)).toBe(true);
    expect(typeof res.body.page).toBe('number');
    expect(typeof res.body.hasNextPage).toBe('boolean');
  });

  it('default page size is 20 and the seed has a second page', async () => {
    const res = await request(app).get('/api/spots');
    expect(res.body.spots).toHaveLength(20);
    expect(res.body.hasNextPage).toBe(true);
  });

  it('page=1 limit=3 returns 3 spots and hasNextPage=true', async () => {
    const res = await request(app).get('/api/spots?page=1&limit=3');
    expect(res.body.spots).toHaveLength(3);
    expect(res.body.page).toBe(1);
    expect(res.body.hasNextPage).toBe(true);
  });

  it('last page has hasNextPage=false', async () => {
    const res = await request(app).get('/api/spots?page=1&limit=1000');
    expect(res.body.hasNextPage).toBe(false);
  });

  it('beyond-last page returns spots: [] and hasNextPage=false', async () => {
    const res = await request(app).get('/api/spots?page=9999&limit=20');
    expect(res.status).toBe(200);
    expect(res.body.spots).toHaveLength(0);
    expect(res.body.hasNextPage).toBe(false);
  });

  it('every spot has the Spot fields and types', async () => {
    const res = await request(app).get('/api/spots?limit=1000');
    res.body.spots.forEach((spot) => {
      expect(typeof spot.id).toBe('string');
      expect(typeof spot.name).toBe('string');
      expect(CATEGORIES).toContain(spot.category);
      expect(spot.rating).toBeGreaterThanOrEqual(0);
      expect(spot.rating).toBeLessThanOrEqual(5);
      expect([1, 2, 3]).toContain(spot.priceLevel);
      expect(typeof spot.lat).toBe('number');
      expect(typeof spot.lng).toBe('number');
      expect(typeof spot.openNow).toBe('boolean');
      expect(typeof spot.photoUrl).toBe('string');
      expect(typeof spot.description).toBe('string');
      expect(typeof spot.updatedAt).toBe('number');
    });
  });

  it('?category=cafe returns only cafe spots', async () => {
    const res = await request(app).get('/api/spots?category=cafe');
    expect(res.body.spots.length).toBeGreaterThan(0);
    res.body.spots.forEach((s) => expect(s.category).toBe('cafe'));
  });

  it('?category=canteen returns only canteen spots', async () => {
    const res = await request(app).get('/api/spots?category=canteen');
    expect(res.body.spots.length).toBeGreaterThan(0);
    res.body.spots.forEach((s) => expect(s.category).toBe('canteen'));
  });

  it('?q= matches name or description, case-insensitive', async () => {
    const res = await request(app).get('/api/spots?q=ESPRESSO');
    expect(res.body.spots.length).toBeGreaterThan(0);
    res.body.spots.forEach((s) => expect(`${s.name} ${s.description}`.toLowerCase()).toContain('espresso'));
  });

  it('?q= and ?category= combine', async () => {
    const res = await request(app).get('/api/spots?q=campus&category=cafe');
    expect(res.body.spots.length).toBeGreaterThan(0);
    res.body.spots.forEach((s) => expect(s.category).toBe('cafe'));
  });
});

describe('CONTRACT: Seed data', () => {
  it('25 spots with fixed ids spot-1 … spot-25', async () => {
    const res = await request(app).get('/api/spots?limit=1000');
    const ids = res.body.spots.map((s) => s.id);
    for (let i = 1; i <= 25; i++) expect(ids).toContain(`spot-${i}`);
  });

  it('the canonical first 8 spots', async () => {
    const res = await request(app).get('/api/spots?limit=8');
    expect(res.body.spots.map((s) => [s.id, s.name, s.category])).toEqual([
      ['spot-1', 'Central Canteen', 'canteen'],
      ['spot-2', 'Espresso Lab', 'cafe'],
      ['spot-3', 'Pizza Stop', 'fastfood'],
      ['spot-4', 'Bread & Butter', 'bakery'],
      ['spot-5', 'The Pub Garden', 'bar'],
      ['spot-6', 'Sushi Box', 'fastfood'],
      ['spot-7', 'Campus Bistro', 'cafe'],
      ['spot-8', "Grandma's Kitchen", 'canteen'],
    ]);
  });

  it('photoUrl = https://picsum.photos/seed/<id>/400/300', async () => {
    const res = await request(app).get(`/api/spots/${SEED_SPOT_2}`);
    expect(res.body.photoUrl).toBe(`https://picsum.photos/seed/${SEED_SPOT_2}/400/300`);
  });

  it('every category is represented and some spots are closed', async () => {
    const { spots } = (await request(app).get('/api/spots?limit=1000')).body;
    CATEGORIES.forEach((c) => expect(spots.some((s) => s.category === c)).toBe(true));
    expect(spots.some((s) => !s.openNow)).toBe(true);
  });

  it('all seed spots lie within 2 km of the campus centre', async () => {
    const { spots } = (await request(app).get('/api/spots?limit=25')).body;
    const toRad = (d) => (d * Math.PI) / 180;
    const km = (s) => {
      const dLat = toRad(s.lat - 44.427);
      const dLng = toRad(s.lng - 26.103);
      const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(44.427)) * Math.cos(toRad(s.lat)) * Math.sin(dLng / 2) ** 2;
      return 6371 * 2 * Math.asin(Math.sqrt(a));
    };
    spots.forEach((s) => expect(km(s)).toBeLessThan(2));
  });
});

describe('CONTRACT: GET /api/spots/:id', () => {
  it('200 with the full Spot for a seeded id', async () => {
    const res = await request(app).get(`/api/spots/${SEED_SPOT_1}`);
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(SEED_SPOT_1);
    expect(typeof res.body.name).toBe('string');
    expect(typeof res.body.updatedAt).toBe('number');
  });

  it('404 envelope for an unknown id', async () => {
    const res = await request(app).get('/api/spots/not-a-real-id');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
    expect(typeof res.body.error.message).toBe('string');
  });
});

describe('CONTRACT: POST /api/spots (auth)', () => {
  it('201 with the full Spot, server-assigned id and updatedAt', async () => {
    const res = await createSpot();
    expect(res.status).toBe(201);
    expect(typeof res.body.id).toBe('string');
    expect(res.body).toMatchObject(VALID_SPOT);
    expect(typeof res.body.updatedAt).toBe('number');
  });

  it('401 envelope with no token', async () => {
    const res = await request(app).post('/api/spots').send(VALID_SPOT);
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it('401 with a malformed token', async () => {
    const res = await request(app).post('/api/spots').set('Authorization', 'Bearer not.a.real.jwt').send(VALID_SPOT);
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it('401 when the scheme is not Bearer', async () => {
    const res = await request(app).post('/api/spots').set('Authorization', `Basic ${token}`).send(VALID_SPOT);
    expect(res.status).toBe(401);
  });

  it('400 validation when name is missing', async () => {
    const res = await authed.post('/api/spots').send({ category: 'cafe' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation when category is missing', async () => {
    const res = await authed.post('/api/spots').send({ name: 'No category spot' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation for an invalid category value', async () => {
    const res = await authed.post('/api/spots').send({ name: 'Bad Cat', category: 'restaurant' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation for a body that is not valid JSON', async () => {
    const res = await authed.post('/api/spots').set('Content-Type', 'application/json').send('{"name":');
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});

describe('CONTRACT: PATCH /api/spots/:id (auth)', () => {
  let patchable;

  beforeAll(async () => {
    patchable = (await createSpot({ name: 'Contract Patchable', category: 'bakery', openNow: false })).body;
  });

  it('200 with the updated Spot', async () => {
    const res = await authed.patch(`/api/spots/${patchable.id}`).send({ openNow: true, name: 'Contract Patched' });
    expect(res.status).toBe(200);
    expect(res.body).toMatchObject({ id: patchable.id, openNow: true, name: 'Contract Patched' });
    expect(res.body.updatedAt).toBeGreaterThan(patchable.updatedAt);
    patchable = res.body;
  });

  it('401 without a token', async () => {
    const res = await request(app).patch(`/api/spots/${patchable.id}`).send({ openNow: false });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it('404 for an unknown id', async () => {
    const res = await authed.patch('/api/spots/ghost-id-contract').send({ name: 'ghost' });
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
  });
});

describe('CONTRACT: PATCH conflict rule (409 on stale updatedAt)', () => {
  it('updatedAt equal to the server copy (tie) → applied, client wins', async () => {
    const spot = (await createSpot({ name: 'Tie' })).body;
    const res = await authed.patch(`/api/spots/${spot.id}`).send({ name: 'Client', updatedAt: spot.updatedAt });
    expect(res.status).toBe(200);
    expect(res.body.name).toBe('Client');
  });

  it('updatedAt older than the server copy → 409 conflict + current server spot, nothing applied', async () => {
    const spot = (await createSpot({ name: 'Base' })).body;
    const server = (await authed.patch(`/api/spots/${spot.id}`).send({ name: 'Server edit' })).body;

    const res = await authed.patch(`/api/spots/${spot.id}`).send({ name: 'Offline edit', updatedAt: spot.updatedAt });
    expect(res.status).toBe(409);
    expect(res.body.error).toEqual({ code: 'conflict', message: expect.any(String) });
    expect(res.body.spot).toEqual(server);
    expect((await request(app).get(`/api/spots/${spot.id}`)).body.name).toBe('Server edit');
  });

  it('outbox replay of a conflicted op (same Idempotency-Key) gets the same 409', async () => {
    const spot = (await createSpot()).body;
    await authed.patch(`/api/spots/${spot.id}`).send({ name: 'Server edit' });
    const opId = crypto.randomUUID();
    const replay = () =>
      authed.patch(`/api/spots/${spot.id}`).set('Idempotency-Key', opId).send({ name: 'Mine', updatedAt: spot.updatedAt });
    const first = await replay();
    const second = await replay();
    expect([first.status, second.status]).toEqual([409, 409]);
    expect(second.body).toEqual(first.body);
  });
});

describe('CONTRACT: DELETE /api/spots/:id (auth)', () => {
  let deletableId;

  beforeAll(async () => {
    deletableId = (await createSpot({ name: 'Contract Deletable', category: 'bar' })).body.id;
  });

  it('401 without a token', async () => {
    const res = await request(app).delete(`/api/spots/${deletableId}`);
    expect(res.status).toBe(401);
  });

  it('404 for an unknown spot', async () => {
    const res = await authed.delete('/api/spots/does-not-exist-contract');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
  });

  it('204 with an empty body', async () => {
    const res = await authed.delete(`/api/spots/${deletableId}`);
    expect(res.status).toBe(204);
    expect(res.text).toBe('');
  });

  it('404 for a spot that was just deleted', async () => {
    const res = await request(app).get(`/api/spots/${deletableId}`);
    expect(res.status).toBe(404);
  });
});

describe('CONTRACT: GET /api/spots/:id/reviews', () => {
  it('returns Review[] for a seeded spot', async () => {
    const res = await request(app).get(`/api/spots/${SEED_SPOT_1}/reviews`);
    expect(res.status).toBe(200);
    expect(Array.isArray(res.body)).toBe(true);
    expect(res.body.length).toBeGreaterThan(0);
  });

  it('each review has the Review fields', async () => {
    const res = await request(app).get(`/api/spots/${SEED_SPOT_1}/reviews`);
    res.body.forEach((r) => {
      expect(typeof r.id).toBe('string');
      expect(r.spotId).toBe(SEED_SPOT_1);
      expect(typeof r.author).toBe('string');
      expect(Number.isInteger(r.stars)).toBe(true);
      expect(r.stars).toBeGreaterThanOrEqual(1);
      expect(r.stars).toBeLessThanOrEqual(5);
      expect(typeof r.text).toBe('string');
      expect(typeof r.createdAt).toBe('number');
    });
  });

  it('404 envelope for an unknown spot', async () => {
    const res = await request(app).get('/api/spots/ghost-contract/reviews');
    expect(res.status).toBe(404);
    expect(res.body.error.code).toBe('not_found');
  });
});

describe('CONTRACT: POST /api/spots/:id/reviews (auth)', () => {
  it('201 with the full Review, author = signed-in displayName', async () => {
    const res = await authed.post(`/api/spots/${SEED_SPOT_2}/reviews`).send({ stars: 5, text: 'Contract test review' });
    expect(res.status).toBe(201);
    expect(res.body).toMatchObject({ spotId: SEED_SPOT_2, stars: 5, text: 'Contract test review', author: 'Demo Student' });
    expect(typeof res.body.id).toBe('string');
    expect(typeof res.body.createdAt).toBe('number');
  });

  it('401 without a token', async () => {
    const res = await request(app).post(`/api/spots/${SEED_SPOT_2}/reviews`).send({ stars: 3, text: 'no auth' });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it('400 validation when stars < 1', async () => {
    const res = await authed.post(`/api/spots/${SEED_SPOT_2}/reviews`).send({ stars: 0, text: 'zero stars' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation when stars > 5', async () => {
    const res = await authed.post(`/api/spots/${SEED_SPOT_2}/reviews`).send({ stars: 6, text: 'too many stars' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('404 when the spot does not exist', async () => {
    const res = await authed.post('/api/spots/nonexistent-contract/reviews').send({ stars: 3, text: 'ghost spot' });
    expect(res.status).toBe(404);
  });
});

describe('CONTRACT: Remote config', () => {
  it('GET /api/config → { flags: { show_new_rating_ui: false } } by default, not cacheable', async () => {
    const res = await request(app).get('/api/config');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ flags: { show_new_rating_ui: false } });
    expect(res.headers['cache-control']).toBe('no-store');
  });

  it('POST /api/config (auth) flips the flag; the next GET sees it', async () => {
    const res = await authed.post('/api/config').send({ flags: { show_new_rating_ui: true } });
    expect(res.status).toBe(200);
    expect((await request(app).get('/api/config')).body.flags.show_new_rating_ui).toBe(true);
    await authed.post('/api/config').send({ flags: { show_new_rating_ui: false } });
  });

  it('POST /api/config is protected (401)', async () => {
    const res = await request(app).post('/api/config').send({ flags: { show_new_rating_ui: true } });
    expect(res.status).toBe(401);
  });

  it('POST /api/config rejects unknown flags and non-booleans (400)', async () => {
    expect((await authed.post('/api/config').send({ flags: { nope: true } })).status).toBe(400);
    expect((await authed.post('/api/config').send({ flags: { show_new_rating_ui: 1 } })).status).toBe(400);
  });
});

describe('CONTRACT: POST /api/ai/describe (SSE)', () => {
  const describeSpot = (body) => authed.post('/api/ai/describe').send(body);

  it('200 text/event-stream: data {delta} frames, then event: done', async () => {
    const res = await describeSpot({
      spot: { name: 'Test Cafe', category: 'cafe', rating: 4.2, priceLevel: 2, openNow: true },
    });
    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toMatch(/^text\/event-stream/);
    const events = parseEvents(res.text);
    const deltas = events.filter((e) => e.event === 'message');
    expect(deltas.length).toBeGreaterThan(0);
    deltas.forEach((e) => expect(typeof e.data.delta).toBe('string'));
    expect(events[events.length - 1].event).toBe('done');
  });

  it('offline (no ANTHROPIC_API_KEY): exactly 4 chunks of the template, naming the spot', async () => {
    const events = parseEvents((await describeSpot({ spot: { name: 'UniCafe', category: 'cafe', openNow: true } })).text);
    expect(events.filter((e) => e.event === 'message')).toHaveLength(4);
    expect(events.map((e) => e.data.delta || '').join('')).toContain('UniCafe');
    expect(events[4]).toEqual({ event: 'done', data: { source: 'template' } });
  });

  it('accepts the spot at the root of the body', async () => {
    const res = await describeSpot({ name: 'Root Spot', category: 'bar', rating: 3.5, priceLevel: 2, openNow: false });
    expect(res.status).toBe(200);
    expect(parseEvents(res.text).pop().event).toBe('done');
  });

  it('401 without a token', async () => {
    const res = await request(app).post('/api/ai/describe').send({ spot: { name: 'x' } });
    expect(res.status).toBe(401);
  });

  it('400 validation (JSON) when the spot has no name', async () => {
    const res = await describeSpot({ spot: { category: 'cafe' } });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation on an empty body', async () => {
    const res = await describeSpot({});
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});

describe('CONTRACT: Error envelope { error: { code, message } }', () => {
  it.each([
    ['404', () => request(app).get('/api/spots/contract-404-test'), 404, 'not_found'],
    ['401', () => request(app).post('/api/spots').send({}), 401, 'unauthorized'],
    ['400', () => request(app).post('/api/auth/login').send({ email: 'x@x.com' }), 400, 'validation'],
    ['unknown route', () => request(app).get('/api/does-not-exist'), 404, 'not_found'],
  ])('%s response carries code + message', async (_, send, status, code) => {
    const res = await send();
    expect(res.status).toBe(status);
    expect(res.body.error).toEqual({ code, message: expect.any(String) });
  });
});

describe('CONTRACT: JWT enforcement', () => {
  it.each([
    ['POST /spots', () => request(app).post('/api/spots').send({ name: 'JWT test', category: 'cafe' })],
    ['PATCH /spots/:id', () => request(app).patch(`/api/spots/${SEED_SPOT_1}`).send({ name: 'no jwt' })],
    ['DELETE /spots/:id', () => request(app).delete(`/api/spots/${SEED_SPOT_1}`)],
    ['POST /spots/:id/reviews', () => request(app).post(`/api/spots/${SEED_SPOT_1}/reviews`).send({ stars: 4 })],
    ['POST /config', () => request(app).post('/api/config').send({ flags: { show_new_rating_ui: true } })],
    ['POST /ai/describe', () => request(app).post('/api/ai/describe').send({ spot: { name: 'x' } })],
  ])('%s → 401 without Authorization', async (_, send) => {
    expect((await send()).status).toBe(401);
  });

  it.each([
    ['tampered signature', () => token.replace(/\.[^.]+$/, '.badsignature')],
    ['random string', () => 'randomgarbage'],
    ['alg: none', () => `${b64({ alg: 'none', typ: 'JWT' })}.${b64(claims())}.`],
    ['wrong secret', () => forge({ alg: 'HS256', typ: 'JWT' }, claims(), 'not-the-secret')],
    ['wrong iss', () => forge({ alg: 'HS256', typ: 'JWT' }, claims({ iss: 'evil' }))],
    ['wrong aud', () => forge({ alg: 'HS256', typ: 'JWT' }, claims({ aud: 'other' }))],
    ['expired', () => forge({ alg: 'HS256', typ: 'JWT' }, claims({ exp: Math.floor(Date.now() / 1000) - 1 }))],
  ])('401 for a token with %s', async (_, makeToken) => {
    const res = await request(app)
      .post('/api/spots')
      .set('Authorization', `Bearer ${makeToken()}`)
      .send({ name: 'JWT', category: 'cafe' });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });
});

describe('CONTRACT: Idempotency-Key replay', () => {
  it('POST /spots: same key → original response', async () => {
    const key = crypto.randomUUID();
    const first = await createSpot({ name: 'Idempotent Contract Spot', category: 'canteen' }).set('Idempotency-Key', key);
    const second = await authed.post('/api/spots').set('Idempotency-Key', key).send({ name: 'Ignored', category: 'bar' });
    expect(first.status).toBe(201);
    expect(second.status).toBe(201);
    expect(second.body).toEqual(first.body);
    expect(second.headers['idempotent-replayed']).toBe('true');
  });

  it('PATCH /spots/:id: same key → original response', async () => {
    const key = crypto.randomUUID();
    const first = await authed.patch(`/api/spots/${SEED_SPOT_1}`).set('Idempotency-Key', key).send({ description: 'idempotent-contract-patch' });
    const second = await authed.patch(`/api/spots/${SEED_SPOT_1}`).set('Idempotency-Key', key).send({ description: 'this-should-not-apply' });
    expect(first.status).toBe(200);
    expect(second.status).toBe(200);
    expect(second.body.description).toBe('idempotent-contract-patch');
  });

  it('DELETE /spots/:id: same key → 204 again', async () => {
    const key = crypto.randomUUID();
    const { id } = (await createSpot({ name: 'Idempotent Delete Spot' })).body;
    expect((await authed.delete(`/api/spots/${id}`).set('Idempotency-Key', key)).status).toBe(204);
    expect((await authed.delete(`/api/spots/${id}`).set('Idempotency-Key', key)).status).toBe(204);
  });

  it('POST review: same key → one review', async () => {
    const key = crypto.randomUUID();
    const first = await authed.post(`/api/spots/${SEED_SPOT_2}/reviews`).set('Idempotency-Key', key).send({ stars: 4 });
    const second = await authed.post(`/api/spots/${SEED_SPOT_2}/reviews`).set('Idempotency-Key', key).send({ stars: 4 });
    expect(second.body).toEqual(first.body);
  });

  it('different keys produce independent results', async () => {
    const r1 = await createSpot({ name: 'Spot Key1' }).set('Idempotency-Key', crypto.randomUUID());
    const r2 = await createSpot({ name: 'Spot Key2' }).set('Idempotency-Key', crypto.randomUUID());
    expect(r1.body.id).not.toBe(r2.body.id);
  });
});

describe('CONTRACT: Chaos headers', () => {
  it('X-Chaos-Delay delays the response', async () => {
    const start = Date.now();
    const res = await request(app).get('/api/health').set('X-Chaos-Delay', '200');
    expect(res.status).toBe(200);
    expect(Date.now() - start).toBeGreaterThanOrEqual(180);
  });

  it('X-Chaos-Status overrides the status code', async () => {
    const res = await request(app).get('/api/health').set('X-Chaos-Status', '503');
    expect(res.status).toBe(503);
  });

  it('X-Chaos-Status 500 on a spots request', async () => {
    const res = await request(app).get('/api/spots').set('X-Chaos-Status', '500');
    expect(res.status).toBe(500);
  });

  it('X-Chaos-Malformed returns invalid JSON', async () => {
    await expect(request(app).get('/api/health').set('X-Chaos-Malformed', '1')).rejects.toThrow(/JSON|parse|token/i);
  });

  it('X-Chaos-Drop closes the socket', async () => {
    await expect(request(app).get('/api/health').set('X-Chaos-Drop', '1')).rejects.toThrow();
  });
});

describe('CONTRACT: Realtime ws://<host>/live', () => {
  let server;
  let wss;

  beforeAll((done) => {
    server = http.createServer(app);
    wss = attachLive(server);
    server.listen(0, done);
  });

  afterAll((done) => {
    wss.close();
    server.closeAllConnections();
    server.close(done);
  });

  it('broadcasts { type: "spot.updated", spot } after a PATCH', async () => {
    const socket = new WebSocket(`ws://localhost:${server.address().port}/live`);
    await new Promise((resolve) => socket.once('open', resolve));
    const message = new Promise((resolve) => socket.once('message', (d) => resolve(JSON.parse(d.toString()))));

    const res = await request(server)
      .patch(`/api/spots/${SEED_SPOT_2}`)
      .set('Authorization', bearer())
      .send({ openNow: true });
    expect(await message).toEqual({ type: 'spot.updated', spot: res.body });
    socket.close();
  });
});
