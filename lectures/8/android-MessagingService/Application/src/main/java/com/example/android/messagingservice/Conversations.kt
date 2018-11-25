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

import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * A simple class that denotes unread conversations and messages. In a real world application,
 * this would be replaced by a content provider that actually gets the unread messages to be
 * shown to the user.
 */
object Conversations {

  /**
   * Set of strings used as messages by the sample.
   */
  private val MESSAGES = arrayOf("Are you at home?",
      "Can you give me a call?",
      "Hey yt?",
      "Don't forget to get some milk on your way back home",
      "Is that project done?",
      "Did you finish the Messaging app yet?")

  /**
   * Senders of the said messages.
   */
  private val PARTICIPANTS = arrayOf("John Smith", "Robert Lawrence", "James Smith", "Jane Doe")

  class Conversation(val conversationId: Int, val participantName: String,
                              messages: List<String>?) {

    /**
     * A given conversation can have a single or multiple messages.
     * Note that the messages are sorted from *newest* to *oldest*
     */
    val messages: List<String>

    val timestamp: Long

    init {
      this.messages = messages ?: emptyList()
      this.timestamp = System.currentTimeMillis()
    }

    override fun toString(): String {
      return "[Conversation: conversationId=" + conversationId +
          ", participantName=" + participantName +
          ", messages=" + messages +
          ", timestamp=" + timestamp + "]"
    }
  }

  fun getUnreadConversations(howManyConversations: Int,
                             messagesPerConversation: Int): Array<Conversation?> {
    val conversations = arrayOfNulls<Conversation>(howManyConversations)
    for (i in 0 until howManyConversations) {
      conversations[i] = Conversation(
          ThreadLocalRandom.current().nextInt(),
          name(), makeMessages(messagesPerConversation))
    }
    return conversations
  }

  private fun makeMessages(messagesPerConversation: Int): List<String> {
    val maxLen = MESSAGES.size
    val messages = ArrayList<String>(messagesPerConversation)
    for (i in 0 until messagesPerConversation) {
      messages.add(MESSAGES[ThreadLocalRandom.current().nextInt(0, maxLen)])
    }
    return messages
  }

  private fun name(): String {
    return PARTICIPANTS[ThreadLocalRandom.current().nextInt(0, PARTICIPANTS.size)]
  }
}
