package com.example.ma.sm.fragments;

import android.support.v4.app.Fragment;

import com.example.ma.sm.StockApp;
import com.squareup.leakcanary.RefWatcher;

public class BaseFragment extends Fragment {

  @Override
  public void onDestroy() {
    super.onDestroy();
    RefWatcher refWatcher = StockApp.getRefWatcher();
    refWatcher.watch(this);
  }
}
