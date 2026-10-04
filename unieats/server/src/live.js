const { WebSocketServer, WebSocket } = require('ws');
const { changes } = require('./store');

function attachLive(server) {
  const wss = new WebSocketServer({ server, path: '/live' });

  const broadcast = (event) => {
    const data = JSON.stringify(event);
    for (const client of wss.clients) {
      if (client.readyState === WebSocket.OPEN) client.send(data);
    }
  };

  wss.on('connection', (socket) => socket.on('error', () => socket.terminate()));
  changes.on('change', broadcast);
  wss.on('close', () => changes.off('change', broadcast));
  return wss;
}

module.exports = { attachLive };
