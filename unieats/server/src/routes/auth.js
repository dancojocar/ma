const { Router } = require('express');
const bcrypt = require('bcryptjs');
const { getUserByEmail } = require('../store');
const { issue } = require('../jwt');
const { sendError, validation } = require('../errors');

const router = Router();

// Compared against when the email is unknown so both failure paths cost one bcrypt check.
const DUMMY_HASH = bcrypt.hashSync('not-a-real-password', 10);

router.post('/login', async (req, res, next) => {
  const { email, password } = req.body || {};
  if (typeof email !== 'string' || typeof password !== 'string' || !email || !password) {
    return validation(res, 'email and password required');
  }

  try {
    const user = getUserByEmail(email);
    const ok = await bcrypt.compare(password, user ? user.passwordHash : DUMMY_HASH);
    if (!user || !ok) {
      return sendError(res, 401, 'unauthorized', 'Invalid email or password');
    }
    return res.json({
      token: issue(user),
      user: { id: user.id, email: user.email, displayName: user.displayName },
    });
  } catch (err) {
    return next(err);
  }
});

module.exports = router;
