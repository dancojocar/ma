package com.example.ma.sm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.ma.sm.database.DBContract.SymbolTable;

public class SymbolDatabase extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "Symbol.db";
  private static final String TAG = SymbolDatabase.class.getSimpleName();
  private static final String TEXT_TYPE = " TEXT";
  private static final String INTEGER_TYPE = " INTEGER";
  private static final String REAL_TYPE = " REAL";
  private static final String COMMA_SEP = ",";

  private static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + SymbolTable.TABLE + " (" +
          SymbolTable._ID + " INTEGER PRIMARY KEY," +
          SymbolTable.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
          SymbolTable.COLUMN_DATE + INTEGER_TYPE + COMMA_SEP +
          SymbolTable.COLUMN_PRICE + REAL_TYPE + COMMA_SEP +
          SymbolTable.COLUMN_QUANTITY + INTEGER_TYPE + COMMA_SEP +
          SymbolTable.COLUMN_PORTFOLIO_ID + INTEGER_TYPE +
          " )";

  private static final String SQL_DELETE_ENTRIES =
      "DROP TABLE IF EXISTS " + SymbolTable.TABLE;


  public SymbolDatabase(Context context) {

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
