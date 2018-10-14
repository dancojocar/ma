package com.example.ma.sm.reader;

import android.util.JsonReader;
import android.util.Log;

import com.example.ma.sm.model.Symbol;

import java.io.IOException;
import java.util.Date;

public class SymbolReader implements ResourceReader<Symbol> {
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String ACQUISITION_DATE = "acquisitionDate";
  public static final String QUANTITY = "quantity";
  public static final String ACQUISITION_PRICE = "acquisitionPrice";
  private static final String TAG = PortfolioReader.class.getSimpleName();

  @Override
  public Symbol read(JsonReader reader) throws IOException {
    Symbol symbol = new Symbol();
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (ID.equals(name)) {
        symbol.setId(reader.nextLong());
      } else if (NAME.equals(name)) {
        symbol.setName(reader.nextString());
      } else if (ACQUISITION_DATE.equals(name)) {
        symbol.setAcquisitionDate(new Date(reader.nextLong()));
      } else if (QUANTITY.equals(name)) {
        symbol.setQuantity(reader.nextInt());
      } else if (ACQUISITION_PRICE.equals(name)) {
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
