package com.example.ma.sm.json.reader;

import android.util.JsonReader;
import android.util.Log;

import com.example.ma.sm.json.Fields;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.Symbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PortfolioReader implements ResourceReader<Portfolio> {
  private static final String TAG = PortfolioReader.class.getSimpleName();
  private ResourceReader<Symbol> resourceReader;

  public PortfolioReader(ResourceReader<Symbol> resourceReader) {
    this.resourceReader = resourceReader;
  }

  @Override
  public Portfolio read(JsonReader reader) throws IOException {
    Portfolio portfolio = new Portfolio();
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (Fields.Portfolio.ID.equals(name)) {
        portfolio.setId(reader.nextLong());
      } else if (Fields.Portfolio.NAME.equals(name)) {
        portfolio.setName(reader.nextString());
      } else if (Fields.Portfolio.TIME.equals(name)) {
        portfolio.setLastModified(new Date(reader.nextLong()));
      } else if (Fields.Portfolio.SYMBOLS.equals(name)) {
        portfolio.setSymbols(readSymbols(reader));
      } else {
        reader.skipValue();
        Log.w(TAG, String.format("Property '%s' ignored", name));
      }
    }
    reader.endObject();
    return portfolio;
  }

  private List<Symbol> readSymbols(JsonReader reader) throws IOException {
    List<Symbol> symbols = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      symbols.add(resourceReader.read(reader));
    }
    reader.endArray();
    return symbols;
  }
}
