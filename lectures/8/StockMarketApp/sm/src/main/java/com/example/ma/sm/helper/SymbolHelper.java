package com.example.ma.sm.helper;

import android.content.ContentValues;

import com.example.ma.sm.database.DBContract;
import com.example.ma.sm.model.Symbol;

public class SymbolHelper {
  public static ContentValues fromSymbol(Symbol symbol, long portfolioId) {
    ContentValues cv = new ContentValues();
    cv.put(DBContract.SymbolTable.COLUMN_NAME, symbol.getName());
    if (symbol.getAcquisitionDate() != null)
      cv.put(DBContract.SymbolTable.COLUMN_DATE, symbol.getAcquisitionDate().getTime());
    cv.put(DBContract.SymbolTable.COLUMN_PRICE, symbol.getAcquisitionPrice());
    cv.put(DBContract.SymbolTable.COLUMN_QUANTITY, symbol.getQuantity());
    cv.put(DBContract.SymbolTable.COLUMN_PORTFOLIO_ID, portfolioId);
    return cv;
  }
}
