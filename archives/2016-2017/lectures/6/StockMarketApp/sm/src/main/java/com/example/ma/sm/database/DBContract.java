package com.example.ma.sm.database;

import android.provider.BaseColumns;

public class DBContract {
  //do not instantiate
  private DBContract() {
  }

  /* Inner class that defines the table contents */
  public static class PortfolioTable implements BaseColumns {
    public static final String TABLE = "portfolio";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAST_MODIFIED = "lastModified";
    public static final String COLUMN_USER_ID= "userId";
  }

  /* Inner class that defines the table contents */
  public static class SymbolTable implements BaseColumns {
    public static final String TABLE = "symbol";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE = "acquisitionDate";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_PRICE = "acquisitionPrice";
    public static final String COLUMN_PORTFOLIO_ID = "portfolioId";
  }

  /* Inner class that defines the table contents */
  public static class UserTable implements BaseColumns {
    public static final String TABLE = "user";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASS = "pass";
    public static final String COLUMN_AUTH_CODE = "authCode";
  }

}
