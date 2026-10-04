const request = require('supertest');
const { Anthropic } = require('@anthropic-ai/sdk');
const app = require('../src/app');
const { MODEL } = require('../src/routes/ai');
const { withAuth } = require('./helpers');
const { parseEvents } = require('./sse');

jest.mock('@anthropic-ai/sdk', () => {
  const stream = jest.fn();
  return { Anthropic: jest.fn(() => ({ beta: { messages: { stream } } })), stream };
});
const { stream: streamMock } = jest.requireMock('@anthropic-ai/sdk');

const api = withAuth(request(app));
const SPOT = { id: 'spot-2', name: 'Espresso Lab', category: 'cafe', rating: 4.5, priceLevel: 2, openNow: false };

const textDelta = (text) => ({ type: 'content_block_delta', index: 0, delta: { type: 'text_delta', text } });

function fakeStream(events, finalMessage, error) {
  return {
    abort: jest.fn(),
    async *[Symbol.asyncIterator]() {
      yield* events;
      if (error) throw error;
    },
    finalMessage: async () => finalMessage,
  };
}

beforeAll(() => {
  process.env.ANTHROPIC_API_KEY = 'test-key';
});

afterAll(() => {
  delete process.env.ANTHROPIC_API_KEY;
});

beforeEach(() => streamMock.mockReset());

describe('POST /api/ai/describe with ANTHROPIC_API_KEY (Claude path, SDK mocked)', () => {
  it('streams the model text deltas in order with the same SSE framing, then done', async () => {
    streamMock.mockReturnValue(
      fakeStream(
        [
          { type: 'message_start', message: {} },
          { type: 'content_block_delta', index: 0, delta: { type: 'thinking_delta', thinking: '' } },
          textDelta('Espresso Lab is '),
          textDelta('a calm cafe.'),
          { type: 'message_stop' },
        ],
        { model: MODEL, stop_reason: 'end_turn' }
      )
    );

    const res = await api.post('/api/ai/describe').send({ spot: SPOT });
    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toMatch(/^text\/event-stream/);
    expect(parseEvents(res.text)).toEqual([
      { event: 'message', data: { delta: 'Espresso Lab is ' } },
      { event: 'message', data: { delta: 'a calm cafe.' } },
      { event: 'done', data: { source: MODEL } },
    ]);
  });

  it('calls the Messages API streaming with claude-sonnet-5-5 and the full spot', async () => {
    streamMock.mockReturnValue(fakeStream([textDelta('ok')], { model: MODEL, stop_reason: 'end_turn' }));
    await api.post('/api/ai/describe').send({ spot: SPOT });

    expect(Anthropic).toHaveBeenCalled();
    const params = streamMock.mock.calls[0][0];
    expect(params.model).toBe('claude-sonnet-5-5');
    expect(typeof params.system).toBe('string');
    const sent = JSON.parse(params.messages[0].content);
    expect(sent).toMatchObject({ name: 'Espresso Lab', category: 'cafe', rating: 4.5, priceLevel: 2, openNow: false });
  });

  it('a refusal ends the stream with event: error (refused)', async () => {
    streamMock.mockReturnValue(fakeStream([], { model: MODEL, stop_reason: 'refusal' }));
    const events = parseEvents((await api.post('/api/ai/describe').send({ spot: SPOT })).text);
    expect(events).toEqual([{ event: 'error', data: { error: { code: 'refused', message: expect.any(String) } } }]);
  });

  it('an upstream failure mid-stream ends with event: error (ai_unavailable)', async () => {
    const spy = jest.spyOn(console, 'error').mockImplementation(() => {});
    streamMock.mockReturnValue(fakeStream([textDelta('Partial')], null, new Error('overloaded')));
    const events = parseEvents((await api.post('/api/ai/describe').send({ spot: SPOT })).text);
    expect(events).toEqual([
      { event: 'message', data: { delta: 'Partial' } },
      { event: 'error', data: { error: { code: 'ai_unavailable', message: expect.any(String) } } },
    ]);
    spy.mockRestore();
  });

  it('validation still answers 400 JSON before any model call', async () => {
    const res = await api.post('/api/ai/describe').send({ spot: { category: 'cafe' } });
    expect(res.status).toBe(400);
    expect(streamMock).not.toHaveBeenCalled();
  });
});
