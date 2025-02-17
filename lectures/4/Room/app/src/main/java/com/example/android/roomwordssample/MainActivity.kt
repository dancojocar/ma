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

package com.example.android.roomwordssample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

  private lateinit var wordViewModel: WordViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
    val adapter = WordListAdapter(this)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    // Get a new or existing ViewModel from the ViewModelProvider.
    wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)

    // Add an observer on the LiveData returned by getAlphabetizedWords.
    // The onChanged() method fires when the observed data changes and the activity is
    // in the foreground.
    wordViewModel.allWords.observe(this) { words ->
      // Update the cached copy of the words in the adapter.
      words?.let { adapter.setWords(it) }
    }

    val fab = findViewById<FloatingActionButton>(R.id.fab)
    fab.setOnClickListener {
      val intent = Intent(this@MainActivity, NewWordActivity::class.java)
      newWordActivityLauncher.launch(intent)
    }
  }

  private val newWordActivityLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        if (data != null) {
          val word = Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY)!!)
          wordViewModel.insert(word)
        }
      } else {
        Toast.makeText(
          applicationContext,
          R.string.empty_not_saved,
          Toast.LENGTH_LONG
        ).show()
      }
    }
}
