package com.example.ma.sm.database.old;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLManager extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "PortfolioEntity.db";
  private static final String TAG = SQLManager.class.getSimpleName();
  private static final String TEXT_TYPE = " TEXT";
  private static final String COMMA_SEP = ",";
  private static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + PortfolioReaderContract.PortfolioEntry.TABLE_NAME + " (" +
          PortfolioReaderContract.PortfolioEntry._ID + " INTEGER PRIMARY KEY," +
          PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
          PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_VALUE + TEXT_TYPE + " )";

  private static final String SQL_DELETE_ENTRIES =
      "DROP TABLE IF EXISTS " + PortfolioReaderContract.PortfolioEntry.TABLE_NAME;


  SQLManager(Context context) {

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
