import 'dart:convert';

/// One Server-Sent Events frame: its `event:` name and its joined `data:`
/// lines.
typedef SseFrame = ({String event, String data});

/// Splits a `text/event-stream` body into frames. A frame ends at a blank
/// line; `event` defaults to `message`; comment lines (`:`) and other fields
/// are ignored. A frame cut off by the end of the stream is dropped, as the
/// SSE spec requires.
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
      event = _fieldValue(line, 'event:');
    } else if (line.startsWith('data:')) {
      data.add(_fieldValue(line, 'data:'));
    }
  }
}

String _fieldValue(String line, String field) {
  final value = line.substring(field.length);
  return value.startsWith(' ') ? value.substring(1) : value;
}
