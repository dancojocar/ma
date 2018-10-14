package com.example.ma.sm.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import timber.log.Timber;

/**
 * Register your app here: https://developers.google.com/mobile/add
 */
public class TokenRefreshListenerService extends FirebaseInstanceIdService {

  @Override
  public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    Timber.v("received a refreshed token: %s", refreshedToken);
    // Non-blocking methods. No need to use AsyncTask or background thread.
    FirebaseMessaging.getInstance().subscribeToTopic("portfolio");
//    FirebaseMessaging.getInstance().unsubscribeFromTopic("portfolio");
  }
}
