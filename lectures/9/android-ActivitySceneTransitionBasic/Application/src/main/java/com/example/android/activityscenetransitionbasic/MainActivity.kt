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

package com.example.android.activityscenetransitionbasic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.example.android.activityscenetransitionbasic.databinding.GridBinding
import com.example.android.activityscenetransitionbasic.databinding.GridItemBinding
import com.squareup.picasso.Picasso

/**
 * Our main Activity in this sample. Displays a grid of items which an image and title. When the
 * user clicks on an item, [DetailActivity] is launched, using the Activity Scene Transitions
 * framework to animatedly do so.
 */
class MainActivity : Activity(), AdapterView.OnItemClickListener {
  private lateinit var binding: GridBinding

  private var mAdapter: GridAdapter? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = GridBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Setup the GridView and set the adapter
    binding.grid.onItemClickListener = this
    mAdapter = GridAdapter()
    binding.grid.adapter = mAdapter
  }

  /**
   * Called when an item in the [android.widget.GridView] is clicked. Here will launch the
   * [DetailActivity], using the Scene Transition animation functionality.
   */
  override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
    val item = adapterView.getItemAtPosition(position) as Item

    // Construct an Intent as normal
    val intent = Intent(this, DetailActivity::class.java)
    intent.putExtra(DetailActivity.EXTRA_PARAM_ID, item.id)

    /**
     * Now create an [android.app.ActivityOptions] instance using the
     * [ActivityOptionsCompat.makeSceneTransitionAnimation] factory
     * method.
     */
    val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
      this,

      // Now we provide a list of Pair items which contain the view we can transitioning
      // from, and the name of the view it is transitioning to, in the launched activity
      Pair(
        view.findViewById(R.id.imageview_item),
        DetailActivity.VIEW_NAME_HEADER_IMAGE
      ),
      Pair(
        view.findViewById(R.id.textview_name),
        DetailActivity.VIEW_NAME_HEADER_TITLE
      )
    )

    // Now we can start the Activity, providing the activity options as a bundle
    ActivityCompat.startActivity(this, intent, activityOptions.toBundle())
  }

  /**
   * [android.widget.BaseAdapter] which displays items.
   */
  private inner class GridAdapter : BaseAdapter() {

    override fun getCount(): Int {
      return Item.ITEMS.size
    }

    override fun getItem(position: Int): Item {
      return Item.ITEMS[position]
    }

    override fun getItemId(position: Int): Long {
      return getItem(position).id.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
      var localView = view
      if (localView == null) {
        localView = layoutInflater.inflate(R.layout.grid_item, viewGroup, false)
      }

      val item = getItem(position)

      // Load the thumbnail image
      val image = localView!!.findViewById<View>(R.id.imageview_item) as ImageView
      Picasso.with(image.context).load(item.thumbnailUrl).into(image)

      // Set the TextView's contents
      val name = localView.findViewById<View>(R.id.textview_name) as TextView
      name.text = item.name

      return localView
    }
  }
}
