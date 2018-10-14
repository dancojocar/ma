package com.example.ma.sm.database.old;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLSample {
  private static final String TAG = SQLSample.class.getSimpleName();

  public void demo(Context context) {
    SQLManager manager = new SQLManager(context);

    //create an entry
    String title = "test";
    createEntry(manager.getWritableDatabase(), title);

    //retrieve info from db
    retrieveEntries(manager.getReadableDatabase(), title);

    //update items
    String replaceTitle = "foo";
    updateEntry(manager.getWritableDatabase(), title, replaceTitle);
    retrieveEntries(manager.getReadableDatabase(), title);

    createEntry(manager.getWritableDatabase(), replaceTitle);
    retrieveEntries(manager.getReadableDatabase(), title);
    //delete
//    delete(manager.getWritableDatabase(), replaceTitle);
    deleteAll(manager.getWritableDatabase());
  }

  private void delete(SQLiteDatabase db, String title) {
    String deleteSelection = PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] deleteArgs = {title};
    db.delete(PortfolioReaderContract.PortfolioEntry.TABLE_NAME, deleteSelection, deleteArgs);
  }

  private void deleteAll(SQLiteDatabase db) {
    db.delete(PortfolioReaderContract.PortfolioEntry.TABLE_NAME, null, null);
  }

  private void updateEntry(SQLiteDatabase db, String fromTitle, String toTitle) {
    ContentValues uv = new ContentValues();
    uv.put(PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE, toTitle);

    String updateSelection = PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] updateArgs = {fromTitle};

    int count = db.update(PortfolioReaderContract.PortfolioEntry.TABLE_NAME, uv, updateSelection, updateArgs);

    Log.i(TAG, "updated: " + count + " entries");
  }

  private void retrieveEntries(SQLiteDatabase db, String titleCriteria) {

    // Define a projection that specifies which columns from the database
// you will actually use after this query.
    String[] projection = {
        PortfolioReaderContract.PortfolioEntry._ID,
        PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE,
        PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_VALUE
    };


// Filter results WHERE "title" = 'test'
    String selection = PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE + " = ?";
    String[] selectionArgs = {titleCriteria};

// Results sorted in the resulting Cursor
    String sortOrder =
        PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_VALUE + " DESC";

    try (Cursor c = db.query(
        PortfolioReaderContract.PortfolioEntry.TABLE_NAME,                     // The table to query
        projection,                               // The columns to return
        selection,                                // The columns for the WHERE clause
        selectionArgs,                            // The values for the WHERE clause
        null,                                     // don't group the rows
        null,                                     // don't filter by row groups
        sortOrder                                 // The sort order
    )) {
      if (c.moveToFirst()) {
        do {
          long pId = c.getLong(c.getColumnIndex(PortfolioReaderContract.PortfolioEntry._ID));
          String pTitle = c.getString(c.getColumnIndex(PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE));
          String pValue = c.getString(c.getColumnIndex(PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_VALUE));
          Log.i(TAG, "id: " + pId + " title: " + pTitle + " value: " + pValue);
        } while (c.moveToNext());
      }
    }
  }

  private void createEntry(SQLiteDatabase db, String title) {
    ContentValues cv = new ContentValues();
    cv.put(PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_TITLE, title);
    cv.put(PortfolioReaderContract.PortfolioEntry.COLUMN_NAME_VALUE, "testValue: " + System.currentTimeMillis());

    //insert the entry
    long rowId = db.insert(PortfolioReaderContract.PortfolioEntry.TABLE_NAME, null, cv);

    Log.i(TAG, "created row: " + rowId);

  }
}
