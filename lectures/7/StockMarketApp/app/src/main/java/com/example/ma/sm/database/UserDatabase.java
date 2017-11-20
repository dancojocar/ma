package com.example.ma.sm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.UserTable;

public class UserDatabase extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 2;
  public static final String DATABASE_NAME = "Users.db";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INTEGER_TYPE = " INTEGER";
  private static final String COMMA_SEP = ",";

  private static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + UserTable.TABLE + " (" +
          UserTable._ID + " INTEGER PRIMARY KEY," +
          UserTable.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
          UserTable.COLUMN_PASS + TEXT_TYPE + COMMA_SEP +
          UserTable.COLUMN_AUTH_CODE + TEXT_TYPE +
          " )";

  private static final String SQL_DELETE_ENTRIES =
      "DROP TABLE IF EXISTS " + UserTable.TABLE;

  public UserDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
    Timber.v("onCreate");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion,
                        int newVersion) {
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
    Timber.v("onUpgrade");
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
    Timber.v("onDowngrade");
  }
}
