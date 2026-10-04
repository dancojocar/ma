const CATEGORIES = ['cafe', 'canteen', 'fastfood', 'bakery', 'bar'];

const SPOT_RULES = {
  name: (v) => typeof v === 'string' && v.trim().length > 0,
  category: (v) => CATEGORIES.includes(v),
  rating: (v) => typeof v === 'number' && v >= 0 && v <= 5,
  priceLevel: (v) => [1, 2, 3].includes(v),
  lat: (v) => typeof v === 'number' && v >= -90 && v <= 90,
  lng: (v) => typeof v === 'number' && v >= -180 && v <= 180,
  openNow: (v) => typeof v === 'boolean',
  photoUrl: (v) => typeof v === 'string',
  description: (v) => typeof v === 'string',
};

function pickSpotFields(body) {
  const fields = {};
  for (const [field, isValid] of Object.entries(SPOT_RULES)) {
    if (body[field] === undefined) continue;
    if (!isValid(body[field])) return { error: `invalid ${field}` };
    fields[field] = body[field];
  }
  return { fields };
}

module.exports = { CATEGORIES, pickSpotFields };
