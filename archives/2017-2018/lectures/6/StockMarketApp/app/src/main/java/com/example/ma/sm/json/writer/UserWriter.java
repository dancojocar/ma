package com.example.ma.sm.json.writer;

import android.util.JsonWriter;

import com.example.ma.sm.json.Fields;
import com.example.ma.sm.model.User;

import java.io.IOException;

public class UserWriter implements ResourceWriter<User, JsonWriter> {
  @Override
  public void write(User u, JsonWriter w) throws IOException {
    w.beginObject();
    w.name(Fields.User.USER).value(u.getUsername());
    w.name(Fields.User.PASS).value(u.getPass());
    w.endObject();
  }
}
