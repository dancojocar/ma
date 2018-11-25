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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput

/**
 * A receiver that gets called when a reply is sent to a given conversationId.
 */
class MessageReplyReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    logd("Received reply")
    if (MessagingService.REPLY_ACTION == intent.action) {
      val conversationId = intent.getIntExtra(MessagingService.CONVERSATION_ID, -1)
      val reply = getMessageText(intent)
      if (conversationId != -1) {
        logd("Got reply ($reply) for ConversationId $conversationId")
        MessageLogger.logMessage(context, "ConversationId: $conversationId received a reply: [$reply]")

        // Update the notification to stop the progress spinner.
        val notificationManager = NotificationManagerCompat.from(context)
        val repliedNotification = NotificationCompat.Builder(context, MessagingService.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(BitmapFactory.decodeResource(
                context.resources, R.drawable.android_contact))
            .setContentText(context.getString(R.string.replied))
            .build()
        notificationManager.notify(conversationId, repliedNotification)
      }
    }
  }

  /**
   * Get the message text from the intent.
   * Note that you should call `RemoteInput#getResultsFromIntent(intent)` to process
   * the RemoteInput.
   */
  private fun getMessageText(intent: Intent): CharSequence? {
    val remoteInput = RemoteInput.getResultsFromIntent(intent)
    return remoteInput?.getCharSequence(
        MessagingService.EXTRA_REMOTE_REPLY)
  }
}
