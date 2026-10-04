function parseEvents(text) {
  return text
    .split('\n\n')
    .filter(Boolean)
    .map((frame) => {
      const lines = frame.split('\n');
      const event = lines.find((l) => l.startsWith('event: '));
      const data = lines.find((l) => l.startsWith('data: '));
      return { event: event ? event.slice(7) : 'message', data: JSON.parse(data.slice(6)) };
    });
}

module.exports = { parseEvents };
