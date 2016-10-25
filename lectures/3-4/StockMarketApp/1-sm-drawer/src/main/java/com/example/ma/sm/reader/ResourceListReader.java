package com.example.ma.sm.reader;

import android.util.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResourceListReader<E> implements ResourceReader<List<E>> {
  private ResourceReader<E> resourceReader;

  public ResourceListReader(ResourceReader<E> resourceReader) {
    this.resourceReader = resourceReader;
  }

  @Override
  public List<E> read(JsonReader reader) throws IOException {
    List<E> list = new ArrayList<E>();
    reader.beginArray();
    while (reader.hasNext()) {
      list.add(resourceReader.read(reader));
    }
    reader.endArray();
    return list;
  }
}
