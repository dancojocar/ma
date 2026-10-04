const TTL_MS = 24 * 60 * 60 * 1000;
const responses = new Map();

function idempotency(req, res, next) {
  const key = req.get('Idempotency-Key');
  if (!key) return next();

  const cached = responses.get(key);
  if (cached && Date.now() - cached.storedAt < TTL_MS) {
    res.set('Idempotent-Replayed', 'true').status(cached.status);
    return cached.body === undefined ? res.end() : res.json(cached.body);
  }

  let body;
  const json = res.json.bind(res);
  res.json = (payload) => {
    body = payload;
    return json(payload);
  };
  res.on('finish', () => {
    if (res.statusCode < 500) {
      responses.set(key, { status: res.statusCode, body, storedAt: Date.now() });
    }
  });
  next();
}

module.exports = { idempotency };
