const request = require('supertest');
const app = require('../src/app');
const seed = require('../src/seed');
const { sign, issue } = require('../src/jwt');
const { DEMO_USER } = require('./helpers');

const DEMO_PASS = 'password';
const now = () => Math.floor(Date.now() / 1000);
const claims = (overrides = {}) => ({
  sub: DEMO_USER.id,
  email: DEMO_USER.email,
  iss: 'unieats',
  aud: 'unieats-app',
  iat: now(),
  exp: now() + 3600,
  ...overrides,
});
const decode = (part) => JSON.parse(Buffer.from(part, 'base64url').toString());

function login(body) {
  return request(app).post('/api/auth/login').send(body);
}

function createWith(authorization) {
  return request(app)
    .post('/api/spots')
    .set('Authorization', authorization)
    .send({ name: 'Auth test', category: 'cafe' });
}

describe('POST /api/auth/login', () => {
  it('200 with token + user for the demo account', async () => {
    const res = await login({ email: DEMO_USER.email, password: DEMO_PASS });
    expect(res.status).toBe(200);
    expect(res.body.user).toEqual(DEMO_USER);
    expect(typeof res.body.token).toBe('string');
  });

  it('issues an HS256 JWT with sub, email, iss, aud, iat and a 1h exp', async () => {
    const { token } = (await login({ email: DEMO_USER.email, password: DEMO_PASS })).body;
    const [header, payload] = token.split('.').map((p, i) => (i < 2 ? decode(p) : p));
    expect(header).toEqual({ alg: 'HS256', typ: 'JWT' });
    expect(payload).toMatchObject({ sub: DEMO_USER.id, email: DEMO_USER.email, iss: 'unieats', aud: 'unieats-app' });
    expect(payload.exp - payload.iat).toBe(3600);
  });

  it('accepts the email in any case and with surrounding spaces', async () => {
    const res = await login({ email: '  Student@UniEats.app ', password: DEMO_PASS });
    expect(res.status).toBe(200);
  });

  it('401 on a wrong password', async () => {
    const res = await login({ email: DEMO_USER.email, password: 'wrong' });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it('401 with the same message for an unknown email (no account enumeration)', async () => {
    const wrongPass = await login({ email: DEMO_USER.email, password: 'wrong' });
    const unknown = await login({ email: 'nobody@unieats.app', password: DEMO_PASS });
    expect(unknown.status).toBe(401);
    expect(unknown.body).toEqual(wrongPass.body);
  });

  it.each([
    ['password missing', { email: DEMO_USER.email }],
    ['email missing', { password: DEMO_PASS }],
    ['non-string password', { email: DEMO_USER.email, password: 123 }],
  ])('400 validation when %s', async (_, body) => {
    const res = await login(body);
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });

  it('stores only a bcrypt hash of the demo password', () => {
    const user = seed.users.find((u) => u.email === DEMO_USER.email);
    expect(user.passwordHash).toMatch(/^\$2[aby]\$10\$/);
    expect(JSON.stringify(seed.users)).not.toContain(`"${DEMO_PASS}"`);
  });
});

describe('Protected routes require a valid Bearer token', () => {
  it.each([
    ['POST /api/spots', () => request(app).post('/api/spots').send({ name: 'x', category: 'cafe' })],
    ['PATCH /api/spots/:id', () => request(app).patch('/api/spots/spot-1').send({ name: 'x' })],
    ['DELETE /api/spots/:id', () => request(app).delete('/api/spots/spot-1')],
    ['POST /api/spots/:id/reviews', () => request(app).post('/api/spots/spot-1/reviews').send({ stars: 4 })],
  ])('%s → 401 envelope + WWW-Authenticate without a token', async (_, send) => {
    const res = await send();
    expect(res.status).toBe(401);
    expect(res.headers['www-authenticate']).toBe('Bearer');
    expect(res.body.error.code).toBe('unauthorized');
    expect(typeof res.body.error.message).toBe('string');
  });

  it('nothing changes when a request is rejected', async () => {
    await request(app).delete('/api/spots/spot-1');
    expect((await request(app).get('/api/spots/spot-1')).status).toBe(200);
  });

  it('reads stay public', async () => {
    expect((await request(app).get('/api/spots')).status).toBe(200);
    expect((await request(app).get('/api/spots/spot-1')).status).toBe(200);
    expect((await request(app).get('/api/spots/spot-1/reviews')).status).toBe(200);
  });

  it('a token from /auth/login works', async () => {
    const { token } = (await login({ email: DEMO_USER.email, password: DEMO_PASS })).body;
    const res = await createWith(`Bearer ${token}`);
    expect(res.status).toBe(201);
  });

  it.each([
    ['wrong scheme', () => `Basic ${issue(DEMO_USER)}`],
    ['empty bearer', () => 'Bearer '],
    ['random string', () => 'Bearer randomgarbage'],
    ['four segments', () => 'Bearer a.b.c.d'],
    ['tampered signature', () => `Bearer ${issue(DEMO_USER).replace(/\.[^.]+$/, '.badsignature')}`],
    ['tampered payload', () => {
      const [h, , s] = issue(DEMO_USER).split('.');
      return `Bearer ${h}.${Buffer.from(JSON.stringify(claims({ sub: 'admin' }))).toString('base64url')}.${s}`;
    }],
    ['alg:none', () => {
      const h = Buffer.from(JSON.stringify({ alg: 'none', typ: 'JWT' })).toString('base64url');
      const p = Buffer.from(JSON.stringify(claims())).toString('base64url');
      return `Bearer ${h}.${p}.`;
    }],
    ['wrong secret', () => `Bearer ${sign(claims(), 'attacker-secret')}`],
    ['wrong issuer', () => `Bearer ${sign(claims({ iss: 'evil' }))}`],
    ['wrong audience', () => `Bearer ${sign(claims({ aud: 'other-app' }))}`],
    ['expired', () => `Bearer ${sign(claims({ iat: now() - 7200, exp: now() - 3600 }))}`],
    ['missing exp', () => `Bearer ${sign(claims({ exp: undefined }))}`],
    ['unknown user', () => `Bearer ${sign(claims({ sub: 'user-999' }))}`],
  ])('401 for a token with %s', async (_, header) => {
    const res = await createWith(header());
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });
});

describe('Reviews are attributed to the signed-in user', () => {
  it('author comes from the token, not the body', async () => {
    const res = await request(app)
      .post('/api/spots/spot-6/reviews')
      .set('Authorization', `Bearer ${issue(DEMO_USER)}`)
      .send({ stars: 4, text: 'Fresh rolls', author: 'Someone Else' });
    expect(res.status).toBe(201);
    expect(res.body.author).toBe(DEMO_USER.displayName);
  });
});
