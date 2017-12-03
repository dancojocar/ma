package com.example.ma.sm.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.ma.sm.database.SymbolDatabase;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.SymbolTable;

public class SymbolContentProvider extends ContentProvider {
  private static final String PROVIDER_NAME = "symbol";
  private static final String URL = "content://" + PROVIDER_NAME + "/symbols";
  public static final Uri CONTENT_URI = Uri.parse(URL);
  private static final int SYMBOLS = 1;
  private static final int SYMBOL_ID = 2;
  // Creates a UriMatcher object.
  private static final UriMatcher uriMatcher;
  private static HashMap<String, String> SYMBOLS_PROJECTION_MAP;

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, "symbols", SYMBOLS);
    uriMatcher.addURI(PROVIDER_NAME, "symbols/#", SYMBOL_ID);
  }

  private SQLiteDatabase db;

  @Override
  public boolean onCreate() {
    Context context = getContext();
    SymbolDatabase dbHelper = new SymbolDatabase(context);
    /**
     * Create a write able database which will trigger its
     * creation if it doesn't already exist.
     */
    db = dbHelper.getWritableDatabase();
    Timber.v("onCreate");
    return db != null;
  }

  @Nullable
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    Timber.v("query");
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables(SymbolTable.TABLE);

    switch (uriMatcher.match(uri)) {
      case SYMBOLS:
        qb.setProjectionMap(SYMBOLS_PROJECTION_MAP);
        break;
      case SYMBOL_ID:
        qb.appendWhere(SymbolTable._ID + "=" + uri.getPathSegments().get(1));
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    if (sortOrder == null || Objects.equals(sortOrder, "")) {
      /**
       * By default sort on symbol names
       */
      sortOrder = SymbolTable.COLUMN_NAME;
    }
    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

    /**
     * register to watch a content URI for changes
     */
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Nullable
  @Override
  public String getType(Uri uri) {
    Timber.v("getType");
    switch (uriMatcher.match(uri)) {
      /**
       * Get all symbols records
       */
      case SYMBOLS:
        return "vnd.android.cursor.dir/vnd.example.symbols";
      /**
       * Get a particular symbol
       */
      case SYMBOL_ID:
        return "vnd.android.cursor.item/vnd.example.symbol";
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
  }

  @Nullable
  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    Timber.v("insert");
    Uri _uri = null;
    long rowID = db.insert(SymbolTable.TABLE, "", contentValues);
    if (rowID > 0) {
      _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
      getContext().getContentResolver().notifyChange(_uri, null);
    }
    return _uri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    Timber.v("delete");
    int count;
    switch (uriMatcher.match(uri)) {
      case SYMBOLS:
        count = db.delete(SymbolTable.TABLE, selection, selectionArgs);
        break;
      case SYMBOL_ID:
        String id = uri.getPathSegments().get(1);
        count = db.delete(SymbolTable.TABLE, SymbolTable._ID + " = " + id +
            (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String selection, String[]
      selectionArgs) {
    Timber.v("update");
    int count;
    switch (uriMatcher.match(uri)) {
      case SYMBOLS:
        count = db.update(SymbolTable.TABLE, contentValues, selection, selectionArgs);
        break;
      case SYMBOL_ID:
        count = db.update(SymbolTable.TABLE, contentValues, SymbolTable._ID + " = " +
            uri.getPathSegments().get(1) +
            (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }
}
