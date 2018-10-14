package com.example.ma.sm.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.example.ma.sm.task.listeners.OnCancellableListener;

public class ErrorHandler extends Handler {
  private Context context;

  public ErrorHandler(Looper mainLooper, Context context) {
    super(mainLooper);
    this.context = context;
  }

  @Override
  public void handleMessage(Message input) {
    new AlertDialog.Builder(context)
        .setTitle("Error")
        .setMessage("Received: " + input.obj.toString())
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            cancel();
          }
        })
        .create()
        .show();
  }

  private void cancel() {
    if (context instanceof OnCancellableListener) {
      OnCancellableListener listener = (OnCancellableListener) context;
      listener.cancel();
    }
  }
}
