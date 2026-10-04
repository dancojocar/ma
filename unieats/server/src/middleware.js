const CHAOS_HEADERS = ['x-chaos-delay', 'x-chaos-status', 'x-chaos-malformed', 'x-chaos-drop'];
const PREVIEW_LENGTH = 300;

function preview(value) {
  if (value === undefined) return '';
  const text = typeof value === 'string' ? value : JSON.stringify(value);
  return text.length > PREVIEW_LENGTH ? `${text.slice(0, PREVIEW_LENGTH)}…` : text;
}

function describeBody(body) {
  if (body === undefined || body === null) return 'none';
  if (Array.isArray(body)) return `array[${body.length}]`;
  if (typeof body === 'object') return `object{${Object.keys(body).join(', ')}}`;
  return typeof body;
}

function describeAuthorization(header) {
  const [scheme, token] = header.split(' ');
  if (!token) return `${scheme} (no token)`;
  const shown = token.length > 20 ? `${token.slice(0, 10)}…${token.slice(-6)}` : token;
  return `${scheme} ${shown} (${token.length} chars)`;
}

function requestLogger(req, res, next) {
  const startedAt = Date.now();
  const id = (req.headers['x-request-id'] || Math.random().toString(36).slice(2, 8)).toString();
  const tag = `[${id}]`;
  let responseBody;
  let bodyParsed = false;

  const originalJson = res.json.bind(res);
  res.json = (body) => {
    responseBody = body;
    return originalJson(body);
  };

  const originalEnd = res.end.bind(res);
  res.end = (chunk, ...rest) => {
    if (responseBody === undefined && chunk && !Buffer.isBuffer(chunk)) responseBody = String(chunk);
    return originalEnd(chunk, ...rest);
  };

  req.on('end', () => {
    bodyParsed = true;
  });

  const chaos = CHAOS_HEADERS.filter((h) => req.headers[h] !== undefined).map((h) => `${h}=${req.headers[h]}`);
  console.log(`\n→ ${tag} ${req.method} ${req.originalUrl}`);
  if (Object.keys(req.query).length) console.log(`  ${tag}   query: ${preview(req.query)}`);
  if (req.headers['content-type']) console.log(`  ${tag}   content-type: ${req.headers['content-type']}`);
  if (req.headers.authorization) console.log(`  ${tag}   authorization: ${describeAuthorization(req.headers.authorization)}`);
  if (req.headers['idempotency-key']) console.log(`  ${tag}   idempotency-key: ${req.headers['idempotency-key']}`);
  if (chaos.length) console.log(`  ${tag}   chaos: ${chaos.join(' ')}`);

  res.on('finish', () => {
    const ms = Date.now() - startedAt;
    const type = res.getHeader('content-type') || 'none';
    const bytes = res.getHeader('content-length') || 0;
    console.log(`← ${tag} ${res.statusCode} ${req.method} ${req.originalUrl} (${ms} ms)`);
    if (req.user) console.log(`  ${tag}   user: ${req.user.id} <${req.user.email}>`);
    if (req.body && bodyParsed && Object.keys(req.body).length) {
      console.log(`  ${tag}   request body: ${preview(req.body)}`);
    }
    console.log(`  ${tag}   response: ${type}, ${bytes} bytes, ${describeBody(responseBody)}`);
    if (responseBody !== undefined) console.log(`  ${tag}   body: ${preview(responseBody)}`);
  });

  res.on('close', () => {
    if (!res.writableFinished) {
      console.log(`✕ ${tag} connection closed before the response finished (${Date.now() - startedAt} ms)`);
    }
  });

  next();
}

function chaos(req, res, next) {
  const delay = parseInt(req.headers['x-chaos-delay'], 10);
  const status = parseInt(req.headers['x-chaos-status'], 10);

  const proceed = () => {
    if (req.headers['x-chaos-drop'] === '1') {
      res.socket?.destroy();
      return;
    }
    if (req.headers['x-chaos-malformed'] === '1') {
      res.setHeader('Content-Type', 'application/json');
      res.end('{invalid json truncated');
      return;
    }
    if (status >= 100 && status <= 599) {
      res.status = () => res;
      res.statusCode = status;
    }
    next();
  };

  if (delay > 0) {
    setTimeout(proceed, delay);
  } else {
    proceed();
  }
}

module.exports = { chaos, requestLogger };
