package com.example.ma.sm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ma.sm.database.PortfolioReaderContract.PortfolioEntry;

public class SQLSample {
  private static final String TAG = SQLSample.class.getSimpleName();

  public void demo(Context context) {
    SQLManager manager = new SQLManager(context);

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
    delete(manager.getWritableDatabase());
  }

  private void delete(SQLiteDatabase db) {
    String deleteSelection = PortfolioEntry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] deleteArgs = {"foo"};
    db.delete(PortfolioEntry.TABLE_NAME, deleteSelection, deleteArgs);
  }

  @NonNull
  private SQLiteDatabase updateEntry(SQLiteDatabase db) {
    ContentValues uv = new ContentValues();
    uv.put(PortfolioEntry.COLUMN_NAME_TITLE, "foo");

    String updateSelection = PortfolioEntry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] updateArgs = {"test"};

    int count = db.update(PortfolioEntry.TABLE_NAME, uv, updateSelection, updateArgs);

    Log.i(TAG, "updated: " + count + " entries");
    return db;
  }

  private void retrieveEntries(SQLiteDatabase db) {

    // Define a projection that specifies which columns from the database
// you will actually use after this query.
    String[] projection = {
        PortfolioEntry._ID,
        PortfolioEntry.COLUMN_NAME_TITLE,
        PortfolioEntry.COLUMN_NAME_VALUE
    };


// Filter results WHERE "title" = 'test'
    String selection = PortfolioEntry.COLUMN_NAME_TITLE + " = ?";
    String[] selectionArgs = {"foo"};

// Results sorted in the resulting Cursor
    String sortOrder =
        PortfolioEntry.COLUMN_NAME_VALUE + " DESC";

    try (Cursor c = db.query(
        PortfolioEntry.TABLE_NAME,                     // The table to query
        projection,                               // The columns to return
        selection,                                // The columns for the WHERE clause
        selectionArgs,                            // The values for the WHERE clause
        null,                                     // don't group the rows
        null,                                     // don't filter by row groups
        sortOrder                                 // The sort order
    )) {
      if (c.moveToFirst()) {
        do {
          long pId = c.getLong(c.getColumnIndex(PortfolioEntry._ID));
          String pValue = c.getString(c.getColumnIndex(PortfolioEntry.COLUMN_NAME_VALUE));
          Log.i(TAG, "id: " + pId + " value: " + pValue);
        } while (c.moveToNext());
      }
    }
  }

  private void createEntry(SQLiteDatabase db) {
    ContentValues cv = new ContentValues();
    cv.put(PortfolioEntry.COLUMN_NAME_TITLE, "test");
    cv.put(PortfolioEntry.COLUMN_NAME_VALUE, "testValue: " + System.currentTimeMillis());

    //insert the entry
    long rowId = db.insert(PortfolioEntry.TABLE_NAME, null, cv);

    Log.i(TAG, "created row: " + rowId);

  }
}
