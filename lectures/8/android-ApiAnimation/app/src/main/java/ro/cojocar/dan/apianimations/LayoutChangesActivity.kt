/*
 * Copyright 2012 The Android Open Source Project
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

package ro.cojocar.dan.apianimations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.*
import android.widget.TextView
import ro.cojocar.dan.apianimations.databinding.ActivityCrossfadeBinding
import ro.cojocar.dan.apianimations.databinding.ActivityLayoutChangesBinding

/**
 * This sample demonstrates how to use system-provided, automatic
 * layout transitions. Layout transitions are animations that occur
 * when views are added to, removed from, or changed within
 * a [ViewGroup].
 *
 *
 *
 * In this sample, the user can add rows to and remove rows
 * from a vertical [android.widget.LinearLayout].
 */
class LayoutChangesActivity : Activity() {
  private lateinit var binding: ActivityLayoutChangesBinding
  /**
   * The container view which has layout change animations turned on.
   * In this sample, this view is a [android.widget.LinearLayout].
   */
  private var mContainerView: ViewGroup? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLayoutChangesBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    mContainerView = binding.container
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    super.onCreateOptionsMenu(menu)
    menuInflater.inflate(R.menu.activity_layout_changes, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        // Navigate "up" the demo structure to the launchpad activity.
        // See http://developer.android.com/design/patterns/navigation.html for more.
        NavUtils.navigateUpTo(
            this,
            Intent(this, AnimationsActivity::class.java)
        )
        return true
      }

      R.id.action_add_item -> {
        // Hide the "empty" view since there is now at least one
        // item in the list.
        binding.empty.visibility = View.GONE
        addItem()
        return true
      }
    }

    return super.onOptionsItemSelected(item)
  }

  private fun addItem() {
    // Instantiate a new "row" view.
    val newView = LayoutInflater.from(this).inflate(
        R.layout.list_item_example,
        mContainerView, false
    ) as ViewGroup

    // Set the text in the new row to a random country.
    val tv = newView.findViewById<TextView>(android.R.id.text1)
    tv.text = COUNTRIES[(Math.random() * COUNTRIES.size).toInt()]

    // Set a click listener for the "X" button in the row
    // that will remove the row.
    val deleteButton = newView.findViewById<View>(R.id.delete_button)
    deleteButton.setOnClickListener {
      // Remove the row from its parent (the container view).
      // Because mContainerView has android:animateLayoutChanges
      // set to true, this removal is automatically animated.
      mContainerView!!.removeView(newView)

      // If there are no rows remaining, show the empty view.
      if (mContainerView!!.childCount == 0) {
        findViewById<View>(android.R.id.empty).visibility = View.VISIBLE
      }
    }

    // Because mContainerView has android:animateLayoutChanges set to true,
    // adding this view is automatically animated.
    mContainerView!!.addView(newView, 0)
  }

  companion object {
    /**
     * A static list of country names.
     */
    private val COUNTRIES = arrayOf(
        "Belgium",
        "France",
        "Italy",
        "Germany",
        "Spain",
        "Austria",
        "Romania",
        "Russia",
        "Poland",
        "Croatia",
        "Greece",
        "Ukraine"
    )
  }
}
