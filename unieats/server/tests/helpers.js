const { issue } = require('../src/jwt');

const DEMO_USER = { id: 'user-1', email: 'student@unieats.app', displayName: 'Demo Student' };

const authHeader = () => `Bearer ${issue(DEMO_USER)}`;

function withAuth(agent) {
  const auth = authHeader();
  return {
    post: (url) => agent.post(url).set('Authorization', auth),
    patch: (url) => agent.patch(url).set('Authorization', auth),
    delete: (url) => agent.delete(url).set('Authorization', auth),
  };
}

module.exports = { DEMO_USER, authHeader, withAuth };
