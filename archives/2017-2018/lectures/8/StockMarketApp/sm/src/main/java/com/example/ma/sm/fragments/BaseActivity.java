package com.example.ma.sm.fragments;

import android.support.v7.app.AppCompatActivity;

import com.example.ma.sm.StockApp;
import com.squareup.leakcanary.RefWatcher;

public class BaseActivity extends AppCompatActivity {
  @Override
  protected void onDestroy() {
    super.onDestroy();
    RefWatcher refWatcher = StockApp.getRefWatcher();
    refWatcher.watch(this);
  }
}
