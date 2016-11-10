package com.example.ma.sm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.ma.sm.util.Constants;

public class NetworkChangeReceiver extends BroadcastReceiver {
  private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = false;
    boolean isWiFi = false;
    boolean isMobile = false;
    if (activeNetwork != null) {
      isConnected = activeNetwork.isConnectedOrConnecting();
      isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
      isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
      if (isConnected && (isWiFi || isMobile)) {
        Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.WIFI, true);
        editor.commit();
      }
    }
    Log.v(TAG, "isConnected: " + isConnected + " isWiFi: " + isWiFi + " isMobile: " + isMobile);
  }
}
