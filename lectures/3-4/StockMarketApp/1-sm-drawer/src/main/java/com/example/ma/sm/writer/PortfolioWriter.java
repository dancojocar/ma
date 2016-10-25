package com.example.ma.sm.writer;

import android.util.JsonWriter;

import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.Symbol;
import com.example.ma.sm.reader.PortfolioReader;
import com.example.ma.sm.reader.SymbolReader;

import java.io.IOException;

public class PortfolioWriter {
  private final JsonWriter w;

  public PortfolioWriter(JsonWriter writer) {
    this.w = writer;
  }

  public String write(Portfolio p) throws IOException {
    w.beginObject();
    w.name(PortfolioReader.ID).value(p.getId());
    w.name(PortfolioReader.NAME).value(p.getName());
    w.name(PortfolioReader.TIME).value(p.getLastModified().getTime());
    w.name(PortfolioReader.SYMBOLS);
    w.flush();
    w.beginArray();
    for (Symbol s : p.getSymbols()) {
      w.beginObject();
      w.name(SymbolReader.ID).value(s.getId());
      w.name(SymbolReader.NAME).value(s.getName());
      w.name(SymbolReader.ACQUISITION_DATE).value(s.getAcquisitionDate().getTime());
      w.name(SymbolReader.ACQUISITION_PRICE).value(s.getAcquisitionPrice());
      w.name(SymbolReader.QUANTITY).value(s.getQuantity());
      w.endObject();
      w.flush();
    }
    w.endArray();
    w.endObject();
    w.flush();
    w.close();
    return null;
  }
}
