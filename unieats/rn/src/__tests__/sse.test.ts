import { createSseParser, type SseEvent } from "../api/sse";

const STREAM =
  'data: {"delta":"Pizza Stop "}\n\n' +
  'data: {"delta":"is open."}\n\n' +
  "event: done\n" +
  'data: {"source":"template"}\n\n';

function parseInChunks(chunkSize: number): SseEvent[] {
  const events: SseEvent[] = [];
  const feed = createSseParser((e) => events.push(e));
  for (let i = 0; i < STREAM.length; i += chunkSize) feed(STREAM.slice(i, i + chunkSize));
  return events;
}

describe("createSseParser", () => {
  it("emits delta frames as 'message' events and the done frame by name", () => {
    expect(parseInChunks(STREAM.length)).toEqual([
      { event: "message", data: '{"delta":"Pizza Stop "}' },
      { event: "message", data: '{"delta":"is open."}' },
      { event: "done", data: '{"source":"template"}' },
    ]);
  });

  it("gives the same events when the network splits frames anywhere", () => {
    expect(parseInChunks(1)).toEqual(parseInChunks(STREAM.length));
    expect(parseInChunks(7)).toEqual(parseInChunks(STREAM.length));
  });

  it("accepts CRLF line endings", () => {
    const events: SseEvent[] = [];
    createSseParser((e) => events.push(e))('event: error\r\ndata: {"error":{"code":"ai_unavailable"}}\r\n\r\n');
    expect(events).toEqual([{ event: "error", data: '{"error":{"code":"ai_unavailable"}}' }]);
  });

  it("waits for the blank line before emitting", () => {
    const events: SseEvent[] = [];
    const feed = createSseParser((e) => events.push(e));
    feed('data: {"delta":"x"}\n');
    expect(events).toHaveLength(0);
    feed("\n");
    expect(events).toHaveLength(1);
  });
});
