const { verify } = require('./jwt');
const { getUserById } = require('./store');
const { sendError } = require('./errors');

function unauthorized(res, message) {
  res.set('WWW-Authenticate', 'Bearer');
  return sendError(res, 401, 'unauthorized', message);
}

function requireAuth(req, res, next) {
  const [scheme, token] = (req.get('Authorization') || '').split(' ');
  if (scheme !== 'Bearer' || !token) return unauthorized(res, 'Missing Bearer token');

  let payload;
  try {
    payload = verify(token);
  } catch (err) {
    return unauthorized(res, err.message);
  }

  const user = getUserById(payload.sub);
  if (!user) return unauthorized(res, 'Unknown user');
  req.user = { id: user.id, email: user.email, displayName: user.displayName };
  return next();
}

module.exports = { requireAuth };
