package com.example.dan.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

private const val SQL_CREATE_ENTRIES =
    """CREATE TABLE IF NOT EXISTS ${NoteContract.NoteEntry.DB_TABLE} (
            ${NoteContract.NoteEntry.COLUMN_ID} INTEGER PRIMARY KEY,
            ${NoteContract.NoteEntry.COLUMN_TITLE} TEXT,
            ${NoteContract.NoteEntry.COLUMN_CONTENT} TEXT);
        """
private const val SQL_DELETE_ENTRIES =
    "DROP TABLE IF EXISTS ${NoteContract.NoteEntry.DB_TABLE}"

class DatabaseHelper(private var context: Context) :
    SQLiteOpenHelper(
        context,
        NoteContract.DB_NAME,
        null,
        NoteContract.DB_VERSION
    ) {

  override fun onCreate(db: SQLiteDatabase?) {
    db!!.execSQL(SQL_CREATE_ENTRIES)
    context.toast(" database is created")
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    db!!.execSQL(SQL_DELETE_ENTRIES)
  }
}

fun Context.toast(message: CharSequence) =
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()