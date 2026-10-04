const { Router } = require('express');
const crypto = require('crypto');
const store = require('../store');
const { idempotency } = require('../idempotency');
const { requireAuth } = require('../auth');
const { pickSpotFields } = require('../validation');
const { sendError, notFound, validation } = require('../errors');

const router = Router();

router.get('/', (req, res) => {
  const page = Math.max(1, parseInt(req.query.page, 10) || 1);
  const limit = Math.max(1, parseInt(req.query.limit, 10) || 20);
  const q = String(req.query.q || '').toLowerCase().trim();
  const category = String(req.query.category || '').toLowerCase().trim();

  let results = store.getSpots();
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
  const spot = store.getSpotById(req.params.id);
  return spot ? res.json(spot) : notFound(res);
});

router.post('/', requireAuth, idempotency, (req, res) => {
  const { fields, error } = pickSpotFields(req.body || {});
  if (error) return validation(res, error);
  if (!fields.name || !fields.category) return validation(res, 'name and category are required');

  const spot = store.addSpot({
    id: crypto.randomUUID(),
    rating: 0,
    priceLevel: 1,
    lat: 0,
    lng: 0,
    openNow: false,
    photoUrl: '',
    description: '',
    ...fields,
    updatedAt: Date.now(),
  });
  return res.status(201).json(spot);
});

router.patch('/:id', requireAuth, idempotency, (req, res) => {
  const current = store.getSpotById(req.params.id);
  if (!current) return notFound(res);

  const body = req.body || {};
  const { fields, error } = pickSpotFields(body);
  if (error) return validation(res, error);

  if (body.updatedAt !== undefined) {
    if (typeof body.updatedAt !== 'number') return validation(res, 'invalid updatedAt');
    if (body.updatedAt < current.updatedAt) {
      return sendError(res, 409, 'conflict', 'Spot was changed by someone else', { spot: current });
    }
  }

  return res.json(store.updateSpot(req.params.id, fields));
});

router.delete('/:id', requireAuth, idempotency, (req, res) => {
  if (!store.deleteSpot(req.params.id)) return notFound(res);
  return res.status(204).end();
});

router.get('/:id/reviews', (req, res) => {
  if (!store.getSpotById(req.params.id)) return notFound(res);
  return res.json(store.getReviewsForSpot(req.params.id));
});

router.post('/:id/reviews', requireAuth, idempotency, (req, res) => {
  if (!store.getSpotById(req.params.id)) return notFound(res);

  const { stars, text = '' } = req.body || {};
  if (!Number.isInteger(stars) || stars < 1 || stars > 5) {
    return validation(res, 'stars must be an integer 1-5');
  }
  if (typeof text !== 'string') return validation(res, 'text must be a string');

  const review = store.addReview({
    id: crypto.randomUUID(),
    spotId: req.params.id,
    author: req.user.displayName,
    stars,
    text,
    createdAt: Date.now(),
  });
  return res.status(201).json(review);
});

module.exports = router;
