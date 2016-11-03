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
import android.util.Log;

import com.example.ma.sm.database.PortfolioDatabase;
import com.example.ma.sm.database.UserDatabase;

import java.util.HashMap;
import java.util.Objects;

import static com.example.ma.sm.database.DBContract.UserTable;

public class UserContentProvider extends ContentProvider {
  private static final String TAG = UserContentProvider.class.getSimpleName();
  private static final String PROVIDER_NAME = "user";
  private static final String URL = "content://" + PROVIDER_NAME + "/users";
  public static final Uri CONTENT_URI = Uri.parse(URL);
  private static final int USERS = 1;
  private static final int USER_ID = 2;
  // Creates a UriMatcher object.
  private static final UriMatcher uriMatcher;
  private static HashMap<String, String> USERS_PROJECTION_MAP;

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, "users", USERS);
    uriMatcher.addURI(PROVIDER_NAME, "user/#", USER_ID);
  }

  private SQLiteDatabase db;

  @Override
  public boolean onCreate() {
    Context context = getContext();
    UserDatabase dbHelper = new UserDatabase(context);
    /**
     * Create a write able database which will trigger its
     * creation if it doesn't already exist.
     */
    db = dbHelper.getWritableDatabase();
    Log.v(TAG, "onCreate");
    return db != null;
  }

  @Nullable
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    Log.v(TAG, "query");
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables(UserTable.TABLE);

    switch (uriMatcher.match(uri)) {
      case USERS:
        qb.setProjectionMap(USERS_PROJECTION_MAP);
        break;
      case USER_ID:
        qb.setTables(UserTable.TABLE);
        qb.appendWhere(UserTable._ID + "=" + uri.getPathSegments().get(1));
        break;
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    if (sortOrder == null || Objects.equals(sortOrder, "")) {
      /**
       * By default sort on user names
       */
      sortOrder = UserTable.COLUMN_NAME;
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
    Log.v(TAG, "getType");
    switch (uriMatcher.match(uri)) {
      /**
       * Get all users records
       */
      case USERS:
        return "vnd.android.cursor.dir/vnd.example.portfolios";
      /**
       * Get a particular user
       */
      case USER_ID:
        return "vnd.android.cursor.item/vnd.example.portfolio";
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
  }

  @Nullable
  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    Log.v(TAG, "insert");
    Uri _uri = null;
    /**
     * Add a new portfolio record
     */
    long rowID = db.insert(UserTable.TABLE, "", contentValues);
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
    Log.v(TAG, "delete");
    int count;
    switch (uriMatcher.match(uri)) {
      case USERS:
        count = db.delete(UserTable.TABLE, selection, selectionArgs);
        break;
      case USER_ID:
        String id = uri.getPathSegments().get(1);
        count = db.delete(UserTable.TABLE, UserTable._ID + " = " + id +
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
    Log.v(TAG, "update");
    int count;
    switch (uriMatcher.match(uri)) {
      case USERS:
        count = db.update(UserTable.TABLE, contentValues, selection, selectionArgs);
        break;
      case USER_ID:
        count = db.update(UserTable.TABLE, contentValues, UserTable._ID + " = " +
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
