const seed = require('./seed');

const spots = seed.spots.map((s) => ({ ...s }));

function getSpots() {
  return spots;
}

function getSpotById(id) {
  return spots.find((s) => s.id === id) || null;
}

module.exports = { getSpots, getSpotById };
