package com.example.ma.sm.json;


public class Fields {
  public static class Portfolio {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TIME = "lastModified";
    public static final String SYMBOLS = "symbols";
  }

  public static class Symbol {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ACQUISITION_DATE = "acquisitionDate";
    public static final String QUANTITY = "quantity";
    public static final String ACQUISITION_PRICE = "acquisitionPrice";
  }

  public static class User {
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String TOKEN = "token";
  }
}
