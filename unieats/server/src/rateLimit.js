const { sendError } = require('./errors');

function rateLimit({ limit, windowMs, key, now = Date.now }) {
  const windows = new Map();

  return (req, res, next) => {
    const id = key(req);
    const t = now();
    let window = windows.get(id);
    if (!window || t >= window.resetAt) {
      window = { count: 0, resetAt: t + windowMs };
      windows.set(id, window);
    }
    window.count += 1;

    res.set('RateLimit-Limit', String(limit));
    res.set('RateLimit-Remaining', String(Math.max(0, limit - window.count)));
    if (window.count > limit) {
      res.set('Retry-After', String(Math.ceil((window.resetAt - t) / 1000)));
      return sendError(res, 429, 'rate_limited', `At most ${limit} requests per minute`);
    }
    return next();
  };
}

module.exports = { rateLimit };
