package com.example.ma.sm.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class NotificationsListenerService extends FirebaseMessagingService {
  private static final String TAG = NotificationsListenerService.class.getSimpleName();

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.v(TAG, "Received a message from: " + remoteMessage.getFrom());
  }
}
