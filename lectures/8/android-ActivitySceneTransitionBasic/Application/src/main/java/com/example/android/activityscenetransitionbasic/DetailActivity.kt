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
import android.os.Bundle
import android.transition.Transition
import android.widget.ImageView
import androidx.core.view.ViewCompat
import com.example.android.activityscenetransitionbasic.databinding.DetailsBinding
import com.squareup.picasso.Picasso

/**
 * Our secondary Activity which is launched from [MainActivity]. Has a simple detail UI
 * which has a large banner image, title and body text.
 */
class DetailActivity : Activity() {
  private lateinit var binding: DetailsBinding

  private var mItem: Item? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DetailsBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Retrieve the correct Item instance, using the ID provided in the Intent
    mItem = Item.getItem(intent.getIntExtra(EXTRA_PARAM_ID, 0))


    /**
     * Set the name of the view's which will be transition to, using the static values above.
     * This could be done in the layout XML, but exposing it via static variables allows easy
     * querying from other Activities
     */
    ViewCompat.setTransitionName(
      binding.imageviewHeader,
      VIEW_NAME_HEADER_IMAGE
    )
    ViewCompat.setTransitionName(
      binding.textviewTitle,
      VIEW_NAME_HEADER_TITLE
    )

    loadItem()
  }

  private fun loadItem() {
    // Set the title TextView to the item's name and author
    binding.textviewTitle.text = getString(R.string.image_header, mItem!!.name, mItem!!.author)

    if (addTransitionListener()) {
      // If we're running on Lollipop and we have added a listener to the shared element
      // transition, load the thumbnail. The listener will load the full-size image when
      // the transition is complete.
      loadThumbnail()
    } else {
      // If all other cases we should just load the full-size image now
      loadFullSizeImage()
    }
  }

  /**
   * Load the item's thumbnail image into our [ImageView].
   */
  private fun loadThumbnail() {
    Picasso.with(binding.imageviewHeader.context)
      .load(mItem!!.thumbnailUrl)
      .noFade()
      .into(binding.imageviewHeader)
  }

  /**
   * Load the item's full-size image into our [ImageView].
   */
  private fun loadFullSizeImage() {
    Picasso.with(binding.imageviewHeader.context)
      .load(mItem!!.photoUrl)
      .noFade()
      .noPlaceholder()
      .into(binding.imageviewHeader)
  }

  /**
   * Try and add a [Transition.TransitionListener] to the entering shared element
   * [Transition]. We do this so that we can load the full-size image after the transition
   * has completed.
   *
   * @return true if we were successful in adding a listener to the enter transition
   */
  private fun addTransitionListener(): Boolean {
    val transition = window.sharedElementEnterTransition

    if (transition != null) {
      // There is an entering shared element transition so add a listener to it
      transition.addListener(object : Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition) {
          // As the transition has ended, we can now load the full-size image
          loadFullSizeImage()

          // Make sure we remove ourselves as a listener
          transition.removeListener(this)
        }

        override fun onTransitionStart(transition: Transition) {
          // No-op
        }

        override fun onTransitionCancel(transition: Transition) {
          // Make sure we remove ourselves as a listener
          transition.removeListener(this)
        }

        override fun onTransitionPause(transition: Transition) {
          // No-op
        }

        override fun onTransitionResume(transition: Transition) {
          // No-op
        }
      })
      return true
    }

    // If we reach here then we have not added a listener
    return false
  }

  companion object {
    // Extra name for the ID parameter
    const val EXTRA_PARAM_ID = "detail:_id"

    // View name of the header image. Used for activity scene transitions
    const val VIEW_NAME_HEADER_IMAGE = "detail:header:image"

    // View name of the header title. Used for activity scene transitions
    const val VIEW_NAME_HEADER_TITLE = "detail:header:title"
  }
}
