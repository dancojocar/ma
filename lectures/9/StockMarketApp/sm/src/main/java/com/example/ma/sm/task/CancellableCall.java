package com.example.ma.sm.task;


import android.content.DialogInterface;

import okhttp3.Call;
import timber.log.Timber;

public class CancellableCall implements DialogInterface.OnCancelListener {

  private Call call;

  public CancellableCall(Call call) {
    this.call = call;
  }

  public void cancel() {
    if (call != null && !call.isCanceled()) {
      Timber.v("Cancel call");
      call.cancel();
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    cancel();
  }
}
