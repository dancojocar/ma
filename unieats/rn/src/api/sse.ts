export interface SseEvent {
  event: string;
  data: string;
}

/**
 * Incremental Server-Sent Events parser: feed it text chunks as they arrive (they may split a frame
 * anywhere) and it emits each complete event. Frames end with a blank line; `event` defaults to "message".
 */
export function createSseParser(onEvent: (event: SseEvent) => void) {
  let buffer = "";
  return (chunk: string) => {
    buffer += chunk.replace(/\r\n/g, "\n");
    let end = buffer.indexOf("\n\n");
    while (end !== -1) {
      const frame = buffer.slice(0, end);
      buffer = buffer.slice(end + 2);
      let event = "message";
      const data: string[] = [];
      for (const line of frame.split("\n")) {
        if (line.startsWith("event:")) event = line.slice(6).trim();
        else if (line.startsWith("data:")) data.push(line.slice(5).replace(/^ /, ""));
      }
      if (data.length > 0) onEvent({ event, data: data.join("\n") });
      end = buffer.indexOf("\n\n");
    }
  };
}
