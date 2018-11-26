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

import android.content.*
import android.os.*
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_message_me.view.*

/**
 * The main fragment that shows the buttons and the text view containing the log.
 */
class MessagingFragment : Fragment(), View.OnClickListener {

  private var mService: Messenger? = null
  private var mBound: Boolean = false

  private lateinit var rootView: View

  private val mConnection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
      mService = Messenger(service)
      mBound = true
      setButtonsState(true)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
      mService = null
      mBound = false
      setButtonsState(false)
    }
  }

  private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (MessageLogger.LOG_KEY == key) {
      rootView.data_port.text = MessageLogger.getAllMessages(context)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    rootView = inflater.inflate(R.layout.fragment_message_me, container, false)

    rootView.data_port.movementMethod = ScrollingMovementMethod()

    rootView.send_1_conversation.setOnClickListener(this)
    rootView.send_2_conversations.setOnClickListener(this)
    rootView.send_1_conversation_3_messages.setOnClickListener(this)
    rootView.clear.setOnClickListener(this)

    setButtonsState(false)

    return rootView
  }

  override fun onClick(view: View) {
    when {
      view === rootView.send_1_conversation -> sendMsg(1, 1)
      view === rootView.send_2_conversations -> sendMsg(2, 1)
      view === rootView.send_1_conversation_3_messages -> sendMsg(1, 3)
      view === rootView.clear -> {
        MessageLogger.clear(context)
        rootView.data_port.text = MessageLogger.getAllMessages(activity)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    activity?.bindService(Intent(activity, MessagingService::class.java), mConnection,
        Context.BIND_AUTO_CREATE)
  }

  override fun onPause() {
    super.onPause()
    MessageLogger.getPrefs(context)?.unregisterOnSharedPreferenceChangeListener(listener)
  }

  override fun onResume() {
    super.onResume()
    rootView.data_port.text = MessageLogger.getAllMessages(activity)
    MessageLogger.getPrefs(context)?.registerOnSharedPreferenceChangeListener(listener)
  }

  override fun onStop() {
    super.onStop()
    if (mBound) {
      activity?.unbindService(mConnection)
      mBound = false
    }
  }

  private fun sendMsg(howManyConversations: Int, messagesPerConversation: Int) {
    if (mBound) {
      val msg = Message.obtain(null, MessagingService.MSG_SEND_NOTIFICATION,
          howManyConversations, messagesPerConversation)
      try {
        mService!!.send(msg)
      } catch (e: RemoteException) {
        loge("Error sending a message", e)
        MessageLogger.logMessage(context, "Error occurred while sending a message.")
      }

    }
  }

  private fun setButtonsState(enable: Boolean) {
    rootView.send_1_conversation.isEnabled = enable
    rootView.send_2_conversations.isEnabled = enable
    rootView.send_1_conversation_3_messages.isEnabled = enable
  }
}
