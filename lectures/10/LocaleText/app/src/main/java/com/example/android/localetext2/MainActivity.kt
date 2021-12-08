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
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.android.localetext2.databinding.ActivityMainBinding

/**
 * This app demonstrates how to localize an app with text, an image,
 * a floating action button, an options menu, and the app bar.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  /**
   * Creates the view with a toolbar for the options menu
   * and a floating action button, and initialize the
   * app data.
   *
   * @param savedInstanceState Bundle with activity's previously saved state.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    setSupportActionBar(binding.toolbar)
    binding.fab.setOnClickListener { showHelp() }
  }

  /**
   * Shows the Help screen.
   */
  private fun showHelp() { // Create the intent.
    val helpIntent = Intent(this, HelpActivity::class.java)
    // Start the HelpActivity.
    startActivity(helpIntent)
  }

  /**
   * Creates the options menu and returns true.
   *
   * @param menu       Options menu
   * @return boolean   True after creating options menu.
   */
  override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  /**
   * Handles options menu item clicks.
   *
   * @param item      Menu item
   * @return boolean  True if menu item is selected.
   */
  override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle options menu item clicks here.
    when (item.itemId) {
      R.id.action_help -> {
        val helpIntent = Intent(this, HelpActivity::class.java)
        startActivity(helpIntent)
        return true
      }
      R.id.action_language -> {
        val languageIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        startActivity(languageIntent)
        return true
      }
      else -> {
      }
    }
    return super.onOptionsItemSelected(item)
  }
}