const crypto = require('crypto');

const SECRET = process.env.JWT_SECRET || 'dev-secret-change-me';
const ISSUER = 'unieats';
const AUDIENCE = 'unieats-app';
const TTL_SECONDS = 60 * 60;
const HEADER = { alg: 'HS256', typ: 'JWT' };

const encode = (value) => Buffer.from(JSON.stringify(value)).toString('base64url');
const hmac = (data, secret) => crypto.createHmac('sha256', secret).update(data).digest();

function decodePart(part) {
  try {
    return JSON.parse(Buffer.from(part, 'base64url').toString());
  } catch {
    throw new Error('malformed token');
  }
}

function sign(payload, secret = SECRET) {
  const unsigned = `${encode(HEADER)}.${encode(payload)}`;
  return `${unsigned}.${hmac(unsigned, secret).toString('base64url')}`;
}

function verify(token, secret = SECRET) {
  const parts = String(token).split('.');
  if (parts.length !== 3) throw new Error('malformed token');
  const [header, body, signature] = parts;

  // The algorithm is fixed here, never taken from the token: this is what rejects alg:none.
  if (decodePart(header).alg !== 'HS256') throw new Error('unsupported algorithm');

  const expected = hmac(`${header}.${body}`, secret);
  const actual = Buffer.from(signature, 'base64url');
  if (actual.length !== expected.length || !crypto.timingSafeEqual(actual, expected)) {
    throw new Error('invalid signature');
  }

  const payload = decodePart(body);
  if (payload.iss !== ISSUER) throw new Error('invalid issuer');
  if (payload.aud !== AUDIENCE) throw new Error('invalid audience');
  if (typeof payload.exp !== 'number' || payload.exp <= Math.floor(Date.now() / 1000)) {
    throw new Error('token expired');
  }
  return payload;
}

function issue(user) {
  const now = Math.floor(Date.now() / 1000);
  return sign({
    sub: user.id,
    email: user.email,
    iss: ISSUER,
    aud: AUDIENCE,
    iat: now,
    exp: now + TTL_SECONDS,
  });
}

module.exports = { sign, verify, issue, ISSUER, AUDIENCE };
