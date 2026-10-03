const express = require('express');
const { chaos } = require('./middleware');
const { sendError } = require('./errors');
const authRouter = require('./routes/auth');
const spotsRouter = require('./routes/spots');
const configRouter = require('./routes/config');

const app = express();

app.use(chaos);
app.use(express.json());

app.get('/api/health', (req, res) => {
  res.json({ ok: true });
});

app.use('/api/auth', authRouter);
app.use('/api/spots', spotsRouter);
app.use('/api/config', configRouter);

app.use((req, res) => {
  sendError(res, 404, 'not_found', 'Route not found');
});

app.use((err, req, res, next) => {
  if (res.headersSent) return next(err);
  if (err.type === 'entity.parse.failed') {
    return sendError(res, 400, 'validation', 'Request body is not valid JSON');
  }
  console.error(err);
  return sendError(res, 500, 'server_error', 'Internal server error');
});

module.exports = app;
