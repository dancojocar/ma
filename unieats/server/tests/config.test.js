const request = require('supertest');
const app = require('../src/app');
const { withAuth } = require('./helpers');

const api = withAuth(request(app));

describe('GET /api/config', () => {
  it('returns the feature flags, show_new_rating_ui off by default', async () => {
    const res = await request(app).get('/api/config');
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ flags: { show_new_rating_ui: false } });
  });

  it('is never cached, so "Fetch & activate" sees the latest value', async () => {
    const res = await request(app).get('/api/config');
    expect(res.headers['cache-control']).toBe('no-store');
  });

  it('honours chaos headers like every other route', async () => {
    const res = await request(app).get('/api/config').set('X-Chaos-Status', '503');
    expect(res.status).toBe(503);
  });
});

describe('POST /api/config', () => {
  afterEach(async () => {
    await api.post('/api/config').send({ flags: { show_new_rating_ui: false } });
  });

  it('flips a flag and the next GET returns it', async () => {
    const res = await api.post('/api/config').send({ flags: { show_new_rating_ui: true } });
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ flags: { show_new_rating_ui: true } });
    expect((await request(app).get('/api/config')).body.flags.show_new_rating_ui).toBe(true);
  });

  it('401 without a token, flag unchanged', async () => {
    const res = await request(app).post('/api/config').send({ flags: { show_new_rating_ui: true } });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
    expect((await request(app).get('/api/config')).body.flags.show_new_rating_ui).toBe(false);
  });

  it.each([
    ['no flags object', {}],
    ['flags is an array', { flags: [true] }],
    ['unknown flag', { flags: { dark_mode: true } }],
    ['non-boolean value', { flags: { show_new_rating_ui: 'yes' } }],
  ])('400 validation for %s', async (_, body) => {
    const res = await api.post('/api/config').send(body);
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});
