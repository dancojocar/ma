package com.example.ma.sm.json.writer;

import java.io.IOException;

public interface ResourceWriter<E, Writer> {
  void write(E e, Writer writer) throws IOException;
}
