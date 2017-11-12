package com.example.ma.sm.json.writer;

import android.util.JsonWriter;

import com.example.ma.sm.json.Fields;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.Symbol;

import java.io.IOException;

public class PortfolioWriter implements ResourceWriter<Portfolio, JsonWriter> {

  @Override
  public void write(Portfolio p, JsonWriter w) throws IOException {
    w.beginObject();
    w.name(Fields.Portfolio.ID).value(p.getId());
    w.name(Fields.Portfolio.NAME).value(p.getName());
    w.name(Fields.Portfolio.TIME).value(p.getLastModified().getTime());
    w.name(Fields.Portfolio.SYMBOLS);
    w.flush();
    w.beginArray();
    for (Symbol s : p.getSymbols()) {
      w.beginObject();
      w.name(Fields.Symbol.ID).value(s.getId());
      w.name(Fields.Symbol.NAME).value(s.getName());
      w.name(Fields.Symbol.ACQUISITION_DATE).value(s.getAcquisitionDate().getTime());
      w.name(Fields.Symbol.ACQUISITION_PRICE).value(s.getAcquisitionPrice());
      w.name(Fields.Symbol.QUANTITY).value(s.getQuantity());
      w.endObject();
      w.flush();
    }
    w.endArray();
    w.endObject();
    w.flush();
    w.close();
  }
}
