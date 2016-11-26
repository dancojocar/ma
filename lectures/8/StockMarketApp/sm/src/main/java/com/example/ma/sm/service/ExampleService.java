package com.example.ma.sm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import timber.log.Timber;

public class ExampleService extends Service {
  int mStartMode;       // indicates how to behave if the service is killed
  IBinder mBinder;      // interface for clients that bind
  boolean mAllowRebind; // indicates whether onRebind should be used

  @Override
  public void onCreate() {

    Timber.v("The service is being created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.v("The service is starting, due to a call to startService()");
    return mStartMode;
  }

  @Override
  public IBinder onBind(Intent intent) {
    Timber.v("A client is binding to the service with bindService()");
    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Timber.v("// All clients have unbound with unbindService()");
    return mAllowRebind;
  }

  @Override
  public void onRebind(Intent intent) {
    Timber.v("A client is binding to the service with bindService(), after onUnbind() has already been called");
  }

  @Override
  public void onDestroy() {
    Timber.v("The service is no longer used and is being destroyed");
  }
}