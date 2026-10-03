const { Router } = require('express');
const { getFlags, setFlags } = require('../store');
const { requireAuth } = require('../auth');
const { validation } = require('../errors');

const router = Router();

router.get('/', (req, res) => {
  res.set('Cache-Control', 'no-store');
  res.json({ flags: getFlags() });
});

router.post('/', requireAuth, (req, res) => {
  const { flags } = req.body || {};
  if (!flags || typeof flags !== 'object' || Array.isArray(flags)) {
    return validation(res, 'body must be { flags: { <name>: boolean } }');
  }
  const known = getFlags();
  for (const [name, value] of Object.entries(flags)) {
    if (!(name in known)) return validation(res, `unknown flag ${name}`);
    if (typeof value !== 'boolean') return validation(res, `flag ${name} must be a boolean`);
  }
  return res.json({ flags: setFlags(flags) });
});

module.exports = router;
