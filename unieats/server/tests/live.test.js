const http = require('http');
const request = require('supertest');
const WebSocket = require('ws');
const app = require('../src/app');
const { attachLive } = require('../src/live');

let server;
let wss;
let baseUrl;

beforeAll((done) => {
  server = http.createServer(app);
  wss = attachLive(server);
  server.listen(0, () => {
    baseUrl = `ws://localhost:${server.address().port}`;
    done();
  });
});

afterAll((done) => {
  wss.close();
  server.closeAllConnections();
  server.close(done);
});

function connect(path = '/live') {
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(`${baseUrl}${path}`);
    socket.once('open', () => resolve(socket));
    socket.once('error', reject);
  });
}

function nextMessage(socket) {
  return new Promise((resolve) => {
    socket.once('message', (data) => resolve(JSON.parse(data.toString())));
  });
}

describe('WebSocket /live', () => {
  let socket;

  beforeEach(async () => {
    socket = await connect();
  });

  afterEach(() => socket.close());

  it('broadcasts spot.created with the new spot', async () => {
    const message = nextMessage(socket);
    const res = await request(server).post('/api/spots').send({ name: 'Live Spot', category: 'cafe' });
    expect(await message).toEqual({ type: 'spot.created', spot: res.body });
  });

  it('broadcasts spot.updated with the updated spot', async () => {
    const message = nextMessage(socket);
    const res = await request(server).patch('/api/spots/spot-2').send({ openNow: false });
    expect(await message).toEqual({ type: 'spot.updated', spot: res.body });
  });

  it('broadcasts spot.deleted with the id', async () => {
    const { id } = (await request(server).post('/api/spots').send({ name: 'Doomed', category: 'bar' })).body;
    const message = nextMessage(socket);
    await request(server).delete(`/api/spots/${id}`).expect(204);
    expect(await message).toEqual({ type: 'spot.deleted', id });
  });

  it('reaches every connected client', async () => {
    const other = await connect();
    const messages = Promise.all([nextMessage(socket), nextMessage(other)]);
    await request(server).patch('/api/spots/spot-3').send({ rating: 4.2 });
    const [a, b] = await messages;
    expect(a).toEqual(b);
    expect(a.type).toBe('spot.updated');
    other.close();
  });

  it('does not broadcast an idempotent replay twice', async () => {
    const received = [];
    socket.on('message', (data) => received.push(JSON.parse(data.toString())));
    const send = () =>
      request(server).patch('/api/spots/spot-4').set('Idempotency-Key', 'live-replay').send({ openNow: true });
    await send();
    await send();
    await new Promise((r) => setTimeout(r, 100));
    expect(received).toHaveLength(1);
  });

  it('only accepts upgrades on /live (not /api/live)', async () => {
    await expect(connect('/api/live')).rejects.toThrow(/400/);
  });
});
