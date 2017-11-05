package com.example.ma.sm.helper;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.ma.sm.model.Portfolio;

import java.util.Date;

import static com.example.ma.sm.database.DBContract.PortfolioTable;

public class PortfolioHelper {
  private static final String TAG = PortfolioHelper.class.getSimpleName();


  public static Portfolio fromCursor(Cursor cursor) {
    Portfolio p = null;
    if (cursor != null) {
      p = new Portfolio();
      p.setId(cursor.getLong(cursor.getColumnIndex(PortfolioTable._ID)));
      p.setName(cursor.getString(cursor.getColumnIndex(PortfolioTable.COLUMN_NAME)));
      p.setLastModified(new Date(cursor.getLong(cursor.getColumnIndex(PortfolioTable.COLUMN_LAST_MODIFIED))));
    }
    return p;
  }

  public static ContentValues fromPortfolio(Portfolio p) {
    ContentValues cv = new ContentValues();
    cv.put(PortfolioTable.COLUMN_NAME, p.getName());
    cv.put(PortfolioTable.COLUMN_LAST_MODIFIED, p.getLastModified().getTime());
    return cv;
  }
}
