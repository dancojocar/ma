const { EventEmitter } = require('events');
const seed = require('./seed');

const changes = new EventEmitter();

const spots = seed.spots.map((s) => ({ ...s }));
const reviews = seed.reviews.map((r) => ({ ...r }));

function getSpots() {
  return spots;
}

function getSpotById(id) {
  return spots.find((s) => s.id === id) || null;
}

function addSpot(spot) {
  spots.push(spot);
  changes.emit('change', { type: 'spot.created', spot });
  return spot;
}

function updateSpot(id, patch) {
  const spot = getSpotById(id);
  if (!spot) return null;
  // Strictly increasing so a client holding the previous version is always detected as stale.
  Object.assign(spot, patch, { updatedAt: Math.max(Date.now(), spot.updatedAt + 1) });
  changes.emit('change', { type: 'spot.updated', spot });
  return spot;
}

function deleteSpot(id) {
  const idx = spots.findIndex((s) => s.id === id);
  if (idx === -1) return false;
  spots.splice(idx, 1);
  for (let i = reviews.length - 1; i >= 0; i--) {
    if (reviews[i].spotId === id) reviews.splice(i, 1);
  }
  changes.emit('change', { type: 'spot.deleted', id });
  return true;
}

function getUserByEmail(email) {
  const normalized = String(email).trim().toLowerCase();
  return seed.users.find((u) => u.email === normalized) || null;
}

function getUserById(id) {
  return seed.users.find((u) => u.id === id) || null;
}

function getReviewsForSpot(spotId) {
  return reviews.filter((r) => r.spotId === spotId);
}

function addReview(review) {
  reviews.push(review);
  return review;
}

module.exports = {
  changes,
  getSpots,
  getSpotById,
  addSpot,
  updateSpot,
  deleteSpot,
  getReviewsForSpot,
  addReview,
  getUserByEmail,
  getUserById,
};
