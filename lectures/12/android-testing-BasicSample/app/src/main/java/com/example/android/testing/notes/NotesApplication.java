package com.example.android.testing.notes;

import android.app.Application;
import android.os.StrictMode;

public class NotesApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
  }
}
