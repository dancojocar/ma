const seed = require('./seed');

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
  return spot;
}

function updateSpot(id, patch) {
  const spot = getSpotById(id);
  if (!spot) return null;
  Object.assign(spot, patch, { updatedAt: Date.now() });
  return spot;
}

function deleteSpot(id) {
  const idx = spots.findIndex((s) => s.id === id);
  if (idx === -1) return false;
  spots.splice(idx, 1);
  for (let i = reviews.length - 1; i >= 0; i--) {
    if (reviews[i].spotId === id) reviews.splice(i, 1);
  }
  return true;
}

function getReviewsForSpot(spotId) {
  return reviews.filter((r) => r.spotId === spotId);
}

function addReview(review) {
  reviews.push(review);
  return review;
}

module.exports = {
  getSpots,
  getSpotById,
  addSpot,
  updateSpot,
  deleteSpot,
  getReviewsForSpot,
  addReview,
};
