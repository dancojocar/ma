package com.example.dan.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class NoteDbManager(context: Context) {
  private val dbHelper: DatabaseHelper = DatabaseHelper(context)
  private val db: SQLiteDatabase by lazy { dbHelper.writableDatabase }

  fun insert(values: ContentValues): Long {
    return db.insert(NoteContract.NoteEntry.DB_TABLE, "", values)
  }

  fun queryAll(): Cursor {
    return db.rawQuery("select * from ${NoteContract.NoteEntry.DB_TABLE}", null)
  }

  fun delete(selection: String, selectionArgs: Array<String>): Int {
    return db.delete(NoteContract.NoteEntry.DB_TABLE, selection, selectionArgs)
  }

  fun update(values: ContentValues, selection: String, selectionArgs: Array<String>): Int {
    return db.update(NoteContract.NoteEntry.DB_TABLE, values, selection, selectionArgs)
  }

  fun close() {
    db.close()
  }
}