/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.gridtopager.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.RecyclerView
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import com.google.samples.gridtopager.MainActivity
import com.google.samples.gridtopager.R
import com.google.samples.gridtopager.adapter.GridAdapter

/**
 * A fragment for displaying a grid of images.
 */
class GridFragment : Fragment() {

  lateinit var recyclerView: RecyclerView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    recyclerView = inflater.inflate(R.layout.fragment_grid, container, false) as RecyclerView
    recyclerView.adapter = GridAdapter(this)

    prepareTransitions()
    postponeEnterTransition()

    return recyclerView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    scrollToPosition()
  }

  /**
   * Scrolls the recycler view to show the last viewed item in the grid. This is important when
   * navigating back from the grid.
   */
  private fun scrollToPosition() {
    recyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
      override fun onLayoutChange(
          view: View,
          left: Int,
          top: Int,
          right: Int,
          bottom: Int,
          oldLeft: Int,
          oldTop: Int,
          oldRight: Int,
          oldBottom: Int) {
        recyclerView.removeOnLayoutChangeListener(this)
        val layoutManager = recyclerView.layoutManager
        val viewAtPosition = layoutManager!!.findViewByPosition(MainActivity.currentPosition)
        // Scroll to position if the view for the current position is null (not currently part of
        // layout manager children), or it's not completely visible.
        if (viewAtPosition == null || layoutManager
                .isViewPartiallyVisible(viewAtPosition, false, true)) {
          recyclerView.post { layoutManager.scrollToPosition(MainActivity.currentPosition) }
        }
      }
    })
  }

  /**
   * Prepares the shared element transition to the pager fragment, as well as the other transitions
   * that affect the flow.
   */
  private fun prepareTransitions() {
    exitTransition = TransitionInflater.from(context)
        .inflateTransition(R.transition.grid_exit_transition)

    // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
    setExitSharedElementCallback(
        object : SharedElementCallback() {
          override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
            // Locate the ViewHolder for the clicked position.
            val selectedViewHolder = recyclerView
                .findViewHolderForAdapterPosition(MainActivity.currentPosition) ?: return

            // Map the first shared element name to the child ImageView.
            sharedElements!![names!![0]] = selectedViewHolder.itemView.findViewById(R.id.card_image)
          }
        })
  }
}
