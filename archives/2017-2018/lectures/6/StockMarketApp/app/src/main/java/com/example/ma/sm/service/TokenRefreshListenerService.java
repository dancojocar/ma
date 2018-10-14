package com.example.ma.sm.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Register your app here: https://developers.google.com/mobile/add
 */
public class TokenRefreshListenerService extends FirebaseInstanceIdService {
  private static final String TAG = "fbService";

  @Override
  public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    Log.v(TAG, "received a refreshed token: " + refreshedToken);
    // Non-blocking methods. No need to use AsyncTask or background thread.
    FirebaseMessaging.getInstance().subscribeToTopic("portfolio");
//    FirebaseMessaging.getInstance().unsubscribeFromTopic("portfolio");
  }
}
