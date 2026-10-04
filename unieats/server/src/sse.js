function openEventStream(res) {
  res.status(200).set({
    'Content-Type': 'text/event-stream; charset=utf-8',
    'Cache-Control': 'no-cache, no-transform',
    Connection: 'keep-alive',
    'X-Accel-Buffering': 'no',
  });
  res.flushHeaders();

  const send = (event, data) => {
    const prefix = event ? `event: ${event}\n` : '';
    res.write(`${prefix}data: ${JSON.stringify(data)}\n\n`);
  };

  return {
    delta: (text) => send(null, { delta: text }),
    done: (data) => {
      send('done', data);
      res.end();
    },
    error: (code, message) => {
      send('error', { error: { code, message } });
      res.end();
    },
  };
}

module.exports = { openEventStream };
