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

import androidx.activity.compose.setContent

class MainActivity : AppCompatActivity() {

  private lateinit var wordViewModel: WordViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)

    setContent {
      MainScreen(wordViewModel = wordViewModel, onAddWord = {
        val intent = Intent(this@MainActivity, NewWordActivity::class.java)
        newWordActivityLauncher.launch(intent)
      }, onEditWord = {
        val intent = Intent(this@MainActivity, NewWordActivity::class.java)
        intent.putExtra(NewWordActivity.EXTRA_REPLY, it)
        updateWordActivityLauncher.launch(intent)
      })
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

  private val updateWordActivityLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        if (data != null) {
          val word = data.getStringExtra(NewWordActivity.EXTRA_REPLY)!!
          val originalWord = data.getStringExtra(NewWordActivity.EXTRA_ORIGINAL_WORD)!!
          wordViewModel.update(word, originalWord)
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
