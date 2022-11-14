/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.example.android.common.logger

import android.app.Activity
import android.content.Context
import android.util.*
import android.widget.TextView

/** Simple TextView which is used to output log data.
 */
class LogView : TextView {

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

  /**
   * Formats the log data and prints it out to the LogView.
   * @param msg The actual message to be logged. The actual message to be logged.
   */
  fun println(msg: String) {

    // In case this was originally called from an AsyncTask or some other off-UI thread,
    // make sure the update occurs within the UI thread.
    (context as Activity).runOnUiThread(Thread(Runnable {
      // Display the text we just generated within the LogView.
      append("\n$msg")
    }))
  }
}
