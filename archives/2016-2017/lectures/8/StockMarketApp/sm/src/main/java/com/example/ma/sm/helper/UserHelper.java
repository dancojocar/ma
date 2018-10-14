package com.example.ma.sm.helper;

import android.content.ContentValues;

import com.example.ma.sm.database.DBContract;
import com.example.ma.sm.model.User;

public class UserHelper {
  public static ContentValues fromUser(User user) {
    ContentValues cv = new ContentValues();
    cv.put(DBContract.UserTable._ID, user.getId());
    cv.put(DBContract.UserTable.COLUMN_NAME, user.getUsername());
    cv.put(DBContract.UserTable.COLUMN_PASS, user.getPass());
    cv.put(DBContract.UserTable.COLUMN_AUTH_CODE, user.getToken());
    return cv;
  }
}

