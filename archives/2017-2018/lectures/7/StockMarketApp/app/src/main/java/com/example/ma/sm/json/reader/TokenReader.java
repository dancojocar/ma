package com.example.ma.sm.json.reader;

import android.util.JsonReader;

import com.example.ma.sm.json.Fields;

import java.io.IOException;

public class TokenReader implements ResourceReader<String> {
  @Override
  public String read(JsonReader r) throws IOException {
    r.beginObject();
    String token = null;
    while (r.hasNext()) {
      String n = r.nextName();
      if (n.endsWith(Fields.User.TOKEN))
        token = r.nextString();
    }
    r.endObject();
    return token;
  }
}
