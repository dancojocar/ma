package com.example.ma.sm.database.old;

import android.provider.BaseColumns;

public class PortfolioReaderContract {
  //do not instantiate
  private PortfolioReaderContract() {
  }

  /* Inner class that defines the table contents */
  public static class PortfolioEntry implements BaseColumns {
    public static final String TABLE_NAME = "portfolio";
    public static final String COLUMN_NAME_TITLE = "name";
    public static final String COLUMN_NAME_VALUE = "value";
  }
}
