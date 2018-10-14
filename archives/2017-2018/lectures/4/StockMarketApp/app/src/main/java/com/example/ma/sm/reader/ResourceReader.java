package com.example.ma.sm.reader;

import android.util.JsonReader;

import java.io.IOException;

interface ResourceReader<E> {
  E read(JsonReader reader) throws IOException;
}
