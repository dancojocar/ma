package com.example.ma.sm.json.reader;

import android.util.JsonReader;
import android.util.Log;

import com.example.ma.sm.json.Fields;
import com.example.ma.sm.model.Symbol;

import java.io.IOException;
import java.util.Date;

public class SymbolReader implements ResourceReader<Symbol> {
  private static final String TAG = PortfolioReader.class.getSimpleName();

  @Override
  public Symbol read(JsonReader reader) throws IOException {
    Symbol symbol = new Symbol();
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (Fields.Symbol.ID.equals(name)) {
        symbol.setId(reader.nextLong());
      } else if (Fields.Symbol.NAME.equals(name)) {
        symbol.setName(reader.nextString());
      } else if (Fields.Symbol.ACQUISITION_DATE.equals(name)) {
        symbol.setAcquisitionDate(new Date(reader.nextLong()));
      } else if (Fields.Symbol.QUANTITY.equals(name)) {
        symbol.setQuantity(reader.nextInt());
      } else if (Fields.Symbol.ACQUISITION_PRICE.equals(name)) {
        symbol.setAcquisitionPrice(reader.nextDouble());
      } else {
        reader.skipValue();
        Log.w(TAG, String.format("Property '%s' ignored", name));
      }
    }
    reader.endObject();
    return symbol;
  }
}
