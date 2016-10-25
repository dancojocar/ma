package com.example.ma.sm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.ma.sm.database.PortfolioReaderContract.PortfolioEntry;

public class SQLManager extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "Portfolio.db";
  private static final String TAG = SQLManager.class.getSimpleName();
  private static final String TEXT_TYPE = " TEXT";
  private static final String COMMA_SEP = ",";
  private static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + PortfolioEntry.TABLE_NAME + " (" +
          PortfolioEntry._ID + " INTEGER PRIMARY KEY," +
          PortfolioEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
          PortfolioEntry.COLUMN_NAME_VALUE + TEXT_TYPE + " )";

  private static final String SQL_DELETE_ENTRIES =
      "DROP TABLE IF EXISTS " + PortfolioEntry.TABLE_NAME;


  public SQLManager(Context context) {

    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
    Log.v(TAG, "onCreate");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion,
                        int newVersion) {
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
    Log.v(TAG, "onUpgrade");
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
    Log.v(TAG, "onDowngrade");
  }
}
