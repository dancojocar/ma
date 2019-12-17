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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.recyclerview.R.string
import com.example.android.recyclerview.WordListAdapter.WordViewHolder
import java.util.*

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
class WordListAdapter(context: Context?, wordList: LinkedList<String>) :
    RecyclerView.Adapter<WordViewHolder>() {
  private val mWordList: LinkedList<String> = wordList
  private val mInflater: LayoutInflater = LayoutInflater.from(context)

  inner class WordViewHolder(itemView: View, adapter: WordListAdapter) :
      RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val wordItemView: TextView = itemView.findViewById<View>(R.id.word) as TextView
    private val mAdapter: WordListAdapter = adapter
    override fun onClick(v: View) { // Prepend "Clicked! " to the text in the view.
      // Original code: wordItemView.setText("Clicked! "+ wordItemView.getText())
      val clickOutput = v.context.getString(string.clicked) +
          wordItemView.text
      wordItemView.text = clickOutput
    }

    /**
     * Creates a new custom view holder to hold the view to display
     * in the RecyclerView.
     *
     * @param itemView The view in which to display the data.
     * @param adapter The adapter that manages the the data and
     * views for the RecyclerView.
     */
    init {
      itemView.setOnClickListener(this)
    }
  }

  /**
   * Inflates an item view and returns a new view holder that contains it.
   * Called when the RecyclerView needs a new view holder to represent an item.
   *
   * @param parent The view group that holds the item views.
   * @param viewType Used to distinguish views, if more than one
   * type of item view is used.
   * @return a view holder.
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder { // Inflate an item view.
    val itemView = mInflater.inflate(R.layout.wordlist_item, parent, false)
    return WordViewHolder(itemView, this)
  }

  /**
   * Sets the contents of an item at a given position in the RecyclerView.
   *
   * @param holder The view holder for that position in the RecyclerView.
   * @param position The position of the item in the RecycerView.
   */
  override fun onBindViewHolder(holder: WordViewHolder, position: Int) { // Retrieve the data for that position.
    val current = mWordList[position]
    // Add the data to the view holder.
    holder.wordItemView.text = current
  }

  /**
   * Returns the size of the container that holds the data.
   *
   * @return Size of the list of data.
   */
  override fun getItemCount(): Int {
    return mWordList.size
  }

}