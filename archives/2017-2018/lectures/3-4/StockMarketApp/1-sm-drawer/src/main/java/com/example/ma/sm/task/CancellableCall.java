package com.example.ma.sm.task;


import android.content.DialogInterface;
import android.util.Log;

import okhttp3.Call;

public class CancellableCall implements DialogInterface.OnCancelListener {
  public static final String TAG = CancellableCall.class.getSimpleName();

  private Call call;

  public CancellableCall(Call call) {
    this.call = call;
  }

  public void cancel() {
    if (call != null && !call.isCanceled()) {
      Log.v(TAG, "Cancel call");
      call.cancel();
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    cancel();
  }
}
