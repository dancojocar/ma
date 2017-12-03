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

import com.example.ma.sm.database.PortfolioDatabase;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.PortfolioTable;

public class PortfolioContentProvider extends ContentProvider {
  private static final String PROVIDER_NAME = "portfolio";
  private static final String URL = "content://" + PROVIDER_NAME + "/portfolios";
  public static final Uri CONTENT_URI = Uri.parse(URL);
  private static final int PORTFOLIOS = 1;
  private static final int PORTFOLIO_ID = 2;
  // Creates a UriMatcher object.
  private static final UriMatcher uriMatcher;
  private static HashMap<String, String> PORTFOLIOS_PROJECTION_MAP;

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, "portfolios", PORTFOLIOS);
    uriMatcher.addURI(PROVIDER_NAME, "portfolios/#", PORTFOLIO_ID);
  }

  private SQLiteDatabase db;

  @Override
  public boolean onCreate() {
    Context context = getContext();
    PortfolioDatabase dbHelper = new PortfolioDatabase(context);
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
    qb.setTables(PortfolioTable.TABLE);

    switch (uriMatcher.match(uri)) {
      case PORTFOLIOS:
        qb.setProjectionMap(PORTFOLIOS_PROJECTION_MAP);
        break;
      case PORTFOLIO_ID:
        qb.appendWhere(PortfolioTable._ID + "=" + uri.getPathSegments().get(1));
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    if (sortOrder == null || Objects.equals(sortOrder, "")) {
      /**
       * By default sort on portfolio names
       */
      sortOrder = PortfolioTable.COLUMN_NAME;
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
       * Get all portfolio records
       */
      case PORTFOLIOS:
        return "vnd.android.cursor.dir/vnd.example.portfolios";
      /**
       * Get a particular portfolio
       */
      case PORTFOLIO_ID:
        return "vnd.android.cursor.item/vnd.example.portfolio";
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
  }

  @Nullable
  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    Timber.v("insert");
    Uri _uri = null;
    /**
     * Add a new portfolio record
     */
    long rowID = db.insert(PortfolioTable.TABLE, "", contentValues);
    /**
     * If record is added successfully
     */
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
      case PORTFOLIOS:
        count = db.delete(PortfolioTable.TABLE, selection, selectionArgs);
        break;
      case PORTFOLIO_ID:
        String id = uri.getPathSegments().get(1);
        count = db.delete(PortfolioTable.TABLE, PortfolioTable._ID + " = " + id +
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
      case PORTFOLIOS:
        count = db.update(PortfolioTable.TABLE, contentValues, selection, selectionArgs);
        break;
      case PORTFOLIO_ID:
        count = db.update(PortfolioTable.TABLE, contentValues, PortfolioTable._ID + " = " +
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
