import 'dart:convert';

/// One Server-Sent Events frame: an optional `event:` name and its joined
/// `data:` lines.
typedef SseFrame = ({String event, String data});

/// Splits a `text/event-stream` body into frames. Frames end at a blank line;
/// `event` defaults to `message`; comment lines (`:`) are ignored.
Stream<SseFrame> parseSse(Stream<List<int>> bytes) async* {
  var event = 'message';
  final data = <String>[];
  await for (final line in const LineSplitter().bind(
    utf8.decoder.bind(bytes),
  )) {
    if (line.isEmpty) {
      if (data.isNotEmpty) yield (event: event, data: data.join('\n'));
      event = 'message';
      data.clear();
    } else if (line.startsWith('event:')) {
      event = line.substring(6).trim();
    } else if (line.startsWith('data:')) {
      data.add(line.substring(5).trimLeft());
    }
  }
  if (data.isNotEmpty) yield (event: event, data: data.join('\n'));
}
