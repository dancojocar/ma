/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.messagingservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.CarExtender
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import java.lang.ref.WeakReference
import java.util.*

class MessagingService : Service() {

  private var mNotificationManager: NotificationManagerCompat? = null

  private val mMessenger = Messenger(IncomingHandler(this))

  override fun onCreate() {
    logd("onCreate")
    mNotificationManager = NotificationManagerCompat.from(applicationContext)
  }

  override fun onBind(intent: Intent): IBinder? {
    logd("onBind")
    return mMessenger.binder
  }

  // Creates an intent that will be triggered when a message is marked as read.
  private fun getMessageReadIntent(id: Int): Intent {
    return Intent()
        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        .setAction(READ_ACTION)
        .putExtra(CONVERSATION_ID, id)
  }

  // Creates an Intent that will be triggered when a voice reply is received.
  private fun getMessageReplyIntent(conversationId: Int): Intent {
    return Intent()
//        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        .setComponent(ComponentName(applicationContext,MessageReplyReceiver::class.java))
        .setAction(REPLY_ACTION)
        .putExtra(CONVERSATION_ID, conversationId)
  }

  private fun sendNotification(howManyConversations: Int, messagesPerConversation: Int) {
    val conversations = Conversations.getUnreadConversations(
        howManyConversations, messagesPerConversation)
    logd("conversations: ${Arrays.asList(conversations)}")
    for (conv in conversations) {
      sendNotificationForConversation(conv!!)
    }
  }

  private fun sendNotificationForConversation(conversation: Conversations.Conversation) {
    // A pending Intent for reads
    val readPendingIntent = PendingIntent.getBroadcast(applicationContext,
        conversation.conversationId,
        getMessageReadIntent(conversation.conversationId),
        PendingIntent.FLAG_UPDATE_CURRENT)

    // Build a RemoteInput for receiving voice input in a Car Notification or text input on
    // devices that support text input (like devices on Android N and above).
    val remoteInput = RemoteInput.Builder(EXTRA_REMOTE_REPLY)
        .setLabel(getString(R.string.reply))
        .build()

    // Building a Pending Intent for the reply action to trigger
    val replyIntent = PendingIntent.getBroadcast(applicationContext,
        conversation.conversationId,
        getMessageReplyIntent(conversation.conversationId),
        PendingIntent.FLAG_UPDATE_CURRENT)

    // Build an Android N compatible Remote Input enabled action.
    val actionReplyByRemoteInput = NotificationCompat.Action.Builder(
        R.drawable.notification_icon, getString(R.string.reply), replyIntent)
        .addRemoteInput(remoteInput)
        .build()

    // Create the UnreadConversation and populate it with the participant name,
    // read and reply intents.
    val unreadConvBuilder = UnreadConversation.Builder(conversation.participantName)
        .setLatestTimestamp(conversation.timestamp)
        .setReadPendingIntent(readPendingIntent)
        .setReplyAction(replyIntent, remoteInput)

    // Note: Add messages from oldest to newest to the UnreadConversation.Builder
    val messageForNotification = StringBuilder()
    val messages = conversation.messages.iterator()
    while (messages.hasNext()) {
      val message = messages.next()
      unreadConvBuilder.addMessage(message)
      messageForNotification.append(message)
      if (messages.hasNext()) {
        messageForNotification.append(EOL)
      }
    }


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create the NotificationChannel
      val name = "Custom notification channel"
      val descriptionText = "Just a demo channel"
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
      mChannel.description = descriptionText
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(mChannel)
    }

    val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setLargeIcon(BitmapFactory.decodeResource(
            applicationContext.resources, R.drawable.android_contact))
        .setContentText(messageForNotification.toString())
        .setWhen(conversation.timestamp)
        .setContentTitle(conversation.participantName)
        .setContentIntent(readPendingIntent)
        .extend(CarExtender()
            .setUnreadConversation(unreadConvBuilder.build())
            .setColor(applicationContext.resources
                .getColor(R.color.default_color_light, theme)))
        .addAction(actionReplyByRemoteInput)

    MessageLogger.logMessage(applicationContext, "Sending notification "
        + conversation.conversationId + " conversation: " + conversation)

    mNotificationManager!!.notify(conversation.conversationId, builder.build())
  }


  /**
   * Handler for incoming messages from clients.
   */
  private class IncomingHandler internal constructor(service: MessagingService) : Handler() {
    private val mReference: WeakReference<MessagingService> = WeakReference(service)

    override fun handleMessage(msg: Message) {
      val service = mReference.get()
      when (msg.what) {
        MSG_SEND_NOTIFICATION -> {
          val howManyConversations = if (msg.arg1 <= 0) 1 else msg.arg1
          val messagesPerConversation = if (msg.arg2 <= 0) 1 else msg.arg2
          logd("Received: $msg $service")
          service?.sendNotification(howManyConversations, messagesPerConversation)
        }
        else -> super.handleMessage(msg)
      }
    }
  }

  companion object {
    private const val EOL = "\n"
    private const val READ_ACTION = "com.example.android.messagingservice.ACTION_MESSAGE_READ"
    const val REPLY_ACTION = "com.example.android.messagingservice.ACTION_MESSAGE_REPLY"
    const val CONVERSATION_ID = "conversation_id"
    const val EXTRA_REMOTE_REPLY = "extra_remote_reply"
    const val MSG_SEND_NOTIFICATION = 1
    const val CHANNEL_ID = "simple1"
  }
}
