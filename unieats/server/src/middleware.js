function chaos(req, res, next) {
  const delay = parseInt(req.headers['x-chaos-delay'], 10);
  const status = parseInt(req.headers['x-chaos-status'], 10);

  const proceed = () => {
    if (req.headers['x-chaos-drop'] === '1') {
      res.socket?.destroy();
      return;
    }
    if (req.headers['x-chaos-malformed'] === '1') {
      res.setHeader('Content-Type', 'application/json');
      res.end('{invalid json truncated');
      return;
    }
    if (status >= 100 && status <= 599) {
      res.status = () => res;
      res.statusCode = status;
    }
    next();
  };

  if (delay > 0) {
    setTimeout(proceed, delay);
  } else {
    proceed();
  }
}

module.exports = { chaos };
