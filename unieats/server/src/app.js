const express = require('express');
const { chaos } = require('./middleware');
const spotsRouter = require('./routes/spots');

const app = express();

app.use(chaos);

app.get('/api/health', (req, res) => {
  res.json({ ok: true });
});

app.use('/api/spots', spotsRouter);

app.use((req, res) => {
  res.status(404).json({ error: { code: 'not_found', message: 'Route not found' } });
});

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: { code: 'server_error', message: 'Internal server error' } });
});

module.exports = app;
