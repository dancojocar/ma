/*
 * Copyright (C) 2017 Google Inc.
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
package com.example.android.localetext2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.android.localetext2.databinding.ActivityHelpBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Help screen that provides a floating action button
 * to start dialing a phone number.
 */
class HelpActivity : AppCompatActivity() {
  private lateinit var binding: ActivityHelpBinding
  /**
   * Creates the view with a floating action button and click listener.
   *
   * @param savedInstanceState Bundle with activity's previously saved state.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHelpBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.fab.setOnClickListener {
      val phoneNumber = getString(R.string.support_phone)
      callSupportCenter(phoneNumber)
    }
  }

  /**
   * Sends an intent to dial the phone number using ACTION_DIAL.
   *
   * @param phoneNumber Phone number string
   */
  private fun callSupportCenter(phoneNumber: String) { // Format the phone number for dialing.
    val formattedNumber = String.format("tel: %s", phoneNumber)
    // Create the intent.
    val dialIntent = Intent(Intent.ACTION_DIAL)
    // Set the formatted phone number as data for the intent.
    dialIntent.data = Uri.parse(formattedNumber)
    // If package resolves to an app, send intent.
    if (dialIntent.resolveActivity(packageManager) != null) {
      startActivity(dialIntent)
    } else {
      Log.e(TAG, getString(R.string.dial_log_message))
    }
  }

  companion object {
    // TAG for the dial logging message.
    private val TAG = HelpActivity::class.java.simpleName
  }
}