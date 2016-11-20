package com.example.ma.sm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.example.ma.sm.database.DBContract.PortfolioTable;

import timber.log.Timber;

public class SQLSample {

  public void demo(Context context) {
    PortfolioDatabase manager = new PortfolioDatabase(context);

    // add info in db
    //get repo in writable mode
    SQLiteDatabase db;

    //create an entry
    createEntry(manager.getWritableDatabase());

    //retrieve info from db
    retrieveEntries(manager.getReadableDatabase());

    //update items
    updateEntry(manager.getWritableDatabase());

    //delete
//    delete(manager.getWritableDatabase());

    //delete the db
    deleteDB(manager.getWritableDatabase());
  }

  private void deleteDB(SQLiteDatabase db) {
    db.execSQL("drop table if exists portfolios.db");
  }

  private void delete(SQLiteDatabase db) {
    String deleteSelection = PortfolioTable.COLUMN_NAME + " LIKE ?";
    String[] deleteArgs = {"foo"};
    db.delete(PortfolioTable.TABLE, deleteSelection, deleteArgs);
  }

  @NonNull
  private SQLiteDatabase updateEntry(SQLiteDatabase db) {
    ContentValues uv = new ContentValues();
    uv.put(PortfolioTable.COLUMN_NAME, "foo");

    String updateSelection = PortfolioTable.COLUMN_NAME + " LIKE ?";
    String[] updateArgs = {"test' "};

    int count = db.update(PortfolioTable.TABLE, uv, updateSelection, updateArgs);

    Timber.d("updated: %d entries", count);
    return db;
  }

  private void retrieveEntries(SQLiteDatabase db) {

    // Define a projection that specifies which columns from the database
// you will actually use after this query.
    String[] projection = {
        PortfolioTable._ID,
        PortfolioTable.COLUMN_NAME,
        PortfolioTable.COLUMN_LAST_MODIFIED
    };


// Filter results WHERE "title" = 'test'
    String selection = PortfolioTable.COLUMN_NAME + " = ?";
    String[] selectionArgs = {"foo"};

// Results sorted in the resulting Cursor
    String sortOrder =
        PortfolioTable.COLUMN_NAME + " DESC";

    try (Cursor c = db.query(
        PortfolioTable.TABLE,                     // The table to query
        projection,                               // The columns to return
        null,                                // The columns for the WHERE clause
        null,                            // The values for the WHERE clause
        null,                                     // don't group the rows
        null,                                     // don't filter by row groups
        sortOrder                                 // The sort order
    )) {
      if (c.moveToFirst()) {
        do {
          long pId = c.getLong(c.getColumnIndex(PortfolioTable._ID));
          String pName = c.getString(c.getColumnIndex(PortfolioTable.COLUMN_NAME));
          Timber.d("id: %d name: %s", pId, pName);
        } while (c.moveToNext());
      }
    }
  }

  private void createEntry(SQLiteDatabase db) {
    ContentValues cv = new ContentValues();
    cv.put(PortfolioTable.COLUMN_NAME, "test");
    cv.put(PortfolioTable.COLUMN_LAST_MODIFIED, System.currentTimeMillis());

    //insert the entry
    long rowId = db.insert(PortfolioTable.TABLE, null, cv);

    Timber.v("created row: %d", rowId);

  }
}
