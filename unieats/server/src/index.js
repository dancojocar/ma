const http = require('http');
const app = require('./app');
const { attachLive } = require('./live');

const PORT = process.env.PORT || 3000;

const server = http.createServer(app);
attachLive(server);

server.listen(PORT, () => {
  console.log(`UniEats server listening on port ${PORT} (WebSocket: ws://localhost:${PORT}/live)`);
});
