package com.example.ma.sm.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


/**
 * https://console.firebase.google.com/project/ubbclasses/notification
 */
public class NotificationsListenerService extends FirebaseMessagingService {
  private static final String TAG = "fbService";

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.v(TAG, "Received a message from: " + remoteMessage.getFrom() +
        " \nmessage id: " + remoteMessage.getMessageId() +
        " \nmessage body: " + remoteMessage.getNotification().getBody()
    );
  }
}
