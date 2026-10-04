function sendError(res, status, code, message, extra = {}) {
  return res.status(status).json({ error: { code, message }, ...extra });
}

const notFound = (res, what = 'Spot') => sendError(res, 404, 'not_found', `${what} not found`);
const validation = (res, message) => sendError(res, 400, 'validation', message);

module.exports = { sendError, notFound, validation };
