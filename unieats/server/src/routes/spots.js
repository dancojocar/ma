const { Router } = require('express');
const { getSpots, getSpotById } = require('../store');

const router = Router();

function notFound(res) {
  return res.status(404).json({ error: { code: 'not_found', message: 'Spot not found' } });
}

router.get('/', (req, res) => {
  const page = Math.max(1, parseInt(req.query.page, 10) || 1);
  const limit = Math.max(1, parseInt(req.query.limit, 10) || 20);
  const q = String(req.query.q || '').toLowerCase().trim();
  const category = String(req.query.category || '').toLowerCase().trim();

  let results = getSpots();
  if (q) {
    results = results.filter(
      (s) => s.name.toLowerCase().includes(q) || s.description.toLowerCase().includes(q)
    );
  }
  if (category) {
    results = results.filter((s) => s.category === category);
  }

  const start = (page - 1) * limit;
  return res.json({
    spots: results.slice(start, start + limit),
    page,
    hasNextPage: start + limit < results.length,
  });
});

router.get('/:id', (req, res) => {
  const spot = getSpotById(req.params.id);
  return spot ? res.json(spot) : notFound(res);
});

module.exports = router;
