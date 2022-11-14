/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.repeatingalarm

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.view.Menu
import com.example.android.common.logd

/**
 * A simple launcher activity containing a summary sample description
 * and a few action bar buttons.
 */
class MainActivity : FragmentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    logd("Started")

    if (supportFragmentManager.findFragmentByTag(FRAG_TAG) == null) {
      val transaction = supportFragmentManager.beginTransaction()
      val fragment = RepeatingAlarmFragment()
      transaction.add(fragment, FRAG_TAG)
      transaction.commit()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    return true
  }

  companion object {
    const val FRAG_TAG = "RepeatingAlarmFragment"
  }
}
