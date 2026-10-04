const http = require('http');
const request = require('supertest');
const app = require('../src/app');
const { buildDescription, splitIntoChunks } = require('../src/routes/ai');
const { rateLimit } = require('../src/rateLimit');
const { authHeader, withAuth } = require('./helpers');
const { parseEvents } = require('./sse');

const api = withAuth(request(app));
const SPOT = { id: 'spot-3', name: 'Pizza Stop', category: 'fastfood', rating: 4.1, priceLevel: 2, openNow: true };

function describe_(body) {
  return api.post('/api/ai/describe').send(body);
}

describe('POST /api/ai/describe without ANTHROPIC_API_KEY (offline template)', () => {
  it('streams Server-Sent Events: 4 delta chunks, then event: done', async () => {
    const res = await describe_({ spot: SPOT });
    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toMatch(/^text\/event-stream/);
    expect(res.headers['cache-control']).toMatch(/no-cache/);

    const events = parseEvents(res.text);
    expect(events).toHaveLength(5);
    events.slice(0, 4).forEach((e) => {
      expect(e.event).toBe('message');
      expect(typeof e.data.delta).toBe('string');
    });
    expect(events[4]).toEqual({ event: 'done', data: { source: 'template' } });
  });

  it('uses the exact frame format data: {"delta": "..."}', async () => {
    const res = await describe_({ spot: SPOT });
    expect(res.text.startsWith('data: {"delta":"Pizza Stop ')).toBe(true);
    expect(res.text.endsWith('event: done\ndata: {"source":"template"}\n\n')).toBe(true);
  });

  it('the chunks concatenate to the deterministic description', async () => {
    const events = parseEvents((await describe_({ spot: SPOT })).text);
    const text = events.filter((e) => e.event === 'message').map((e) => e.data.delta).join('');
    expect(text).toBe(buildDescription(SPOT));
    expect(text).toContain('Pizza Stop');
    expect(text).toContain('rated 4.1/5');
    expect(text).toContain('mid-range');
    expect(text).toContain('open right now');
  });

  it('reflects openNow=false', async () => {
    const events = parseEvents((await describe_({ spot: { ...SPOT, openNow: false } })).text);
    const text = events.map((e) => e.data.delta || '').join('');
    expect(text).toContain('closed at the moment');
  });

  it('accepts the spot at the root of the body too', async () => {
    const res = await describe_(SPOT);
    expect(res.status).toBe(200);
    expect(parseEvents(res.text).pop().event).toBe('done');
  });

  it('delivers chunks incrementally, not as one buffered body', async () => {
    const server = http.createServer(app);
    await new Promise((resolve) => server.listen(0, resolve));
    const { port } = server.address();
    const arrivals = [];
    await new Promise((resolve, reject) => {
      const req = http.request(
        {
          port,
          agent: false,
          method: 'POST',
          path: '/api/ai/describe',
          headers: {
            'Content-Type': 'application/json',
            Authorization: authHeader(),
          },
        },
        (res) => {
          res.on('data', () => arrivals.push(Date.now()));
          res.on('end', resolve);
        }
      );
      req.on('error', reject);
      req.end(JSON.stringify({ spot: SPOT }));
    });
    await new Promise((resolve) => server.close(resolve));
    expect(arrivals.length).toBeGreaterThanOrEqual(4);
    expect(arrivals[arrivals.length - 1] - arrivals[0]).toBeGreaterThan(0);
  });

  it('401 without a token', async () => {
    const res = await request(app).post('/api/ai/describe').send({ spot: SPOT });
    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe('unauthorized');
  });

  it.each([
    ['empty body', {}],
    ['spot without a name', { spot: { category: 'cafe' } }],
    ['blank name', { spot: { name: '  ' } }],
  ])('400 validation (JSON, not a stream) for %s', async (_, body) => {
    const res = await describe_(body);
    expect(res.status).toBe(400);
    expect(res.headers['content-type']).toMatch(/json/);
    expect(res.body.error.code).toBe('validation');
  });

  it('400 validation for a body that is not valid JSON', async () => {
    const res = await api.post('/api/ai/describe').set('Content-Type', 'application/json').send('{"spot":');
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('validation');
  });
});

describe('splitIntoChunks', () => {
  it.each([4, 5, 9, 30])('always yields exactly 4 non-empty chunks (%i words)', (n) => {
    const text = Array.from({ length: n }, (_, i) => `w${i}`).join(' ');
    const chunks = splitIntoChunks(text, 4);
    expect(chunks).toHaveLength(4);
    chunks.forEach((c) => expect(c.trim()).not.toBe(''));
    expect(chunks.join('')).toBe(text);
  });
});

describe('rate limit: 10 requests per minute per user', () => {
  it('the 11th describe request in a minute gets 429 with Retry-After', async () => {
    const statuses = [];
    for (let i = 0; i < 11; i++) {
      statuses.push((await describe_({ spot: SPOT })).status);
    }
    // Earlier tests in this file already used part of the budget.
    expect(statuses).toContain(429);
    const limited = await describe_({ spot: SPOT });
    expect(limited.status).toBe(429);
    expect(limited.body.error.code).toBe('rate_limited');
    expect(Number(limited.headers['retry-after'])).toBeGreaterThan(0);
    expect(limited.headers['ratelimit-remaining']).toBe('0');
  });

  it('counts each user separately and resets after the window', () => {
    let t = 0;
    const limiter = rateLimit({ limit: 2, windowMs: 60_000, key: (req) => req.user.id, now: () => t });
    const hit = (id) => {
      let status = 200;
      const res = { set: () => res, status: (s) => { status = s; return res; }, json: () => res };
      limiter({ user: { id } }, res, () => {});
      return status;
    };
    expect([hit('a'), hit('a'), hit('a')]).toEqual([200, 200, 429]);
    expect(hit('b')).toBe(200);
    t = 60_000;
    expect(hit('a')).toBe(200);
  });
});
