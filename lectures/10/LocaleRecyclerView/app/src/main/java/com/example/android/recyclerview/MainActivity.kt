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
package com.example.android.recyclerview

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.recyclerview.R.string
import com.example.android.recyclerview.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

/**
 * Implements a basic RecyclerView that displays a list of generated words.
 * - Clicking an item marks it as clicked.
 * - Clicking the fab button adds a new word to the list.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private var mRecyclerView: RecyclerView? = null
  private var mAdapter: WordListAdapter? = null
  private val mWordList = LinkedList<String>()
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    // Put initial data into the word list.
    for (i in 0..19) { // Original code: mWordList.addLast("Word " + i)
      mWordList.addLast(String.format(resources.getString(string.word), i))
    }
    // Create recycler view.
    mRecyclerView = binding.recyclerview
    // Create an adapter and supply the data to be displayed.
    mAdapter = WordListAdapter(this, mWordList)
    // Connect the adapter with the recycler view.
    mRecyclerView!!.adapter = mAdapter
    // Give the recycler view a default layout manager.
    mRecyclerView!!.layoutManager = LinearLayoutManager(this)
    // Add a floating action click handler for adding new entries.
    binding.fab.setOnClickListener {
      val wordListSize = mWordList.size
      // Add a new word to the wordList.
      // Original code: mWordList.addLast("+ Word " + wordListSize)
      mWordList.addLast(
        "+ " + resources.getString(string.word, wordListSize)
      )
      // Notify the adapter, that the data has changed.
      mRecyclerView!!.adapter!!.notifyItemInserted(wordListSize)
      // Scroll to the bottom.
      mRecyclerView!!.smoothScrollToPosition(wordListSize)
    }
  }

  /**
   * Creates the options menu and returns true.
   *
   * @param menu       Options menu
   * @return boolean   True after creating options menu.
   */
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
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