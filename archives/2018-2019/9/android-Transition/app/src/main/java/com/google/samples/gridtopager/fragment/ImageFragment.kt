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

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.samples.gridtopager.R

/**
 * A fragment for displaying an image.
 */
class ImageFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_image, container, false)

    val arguments = arguments
    @DrawableRes val imageRes = arguments!!.getInt(KEY_IMAGE_RES)

    // Just like we do when binding views at the grid, we set the transition name to be the string
    // value of the image res.
    view.findViewById<View>(R.id.image).transitionName = imageRes.toString()

    // Load the image with Glide to prevent OOM error when the image drawables are very large.
    Glide.with(this)
        .load(imageRes)
        .listener(object : RequestListener<Drawable> {
          override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
            // startPostponedEnterTransition() should also be called on it to get the transition
            // going in case of a failure.
            parentFragment!!.startPostponedEnterTransition()
            return false
          }

          override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
            // startPostponedEnterTransition() should also be called on it to get the transition
            // going when the image is ready.
            parentFragment!!.startPostponedEnterTransition()
            return false
          }
        })
        .into(view.findViewById<View>(R.id.image) as ImageView)
    return view
  }

  companion object {

    private val KEY_IMAGE_RES = "com.google.samples.gridtopager.key.imageRes"

    fun newInstance(@DrawableRes drawableRes: Int): ImageFragment {
      val fragment = ImageFragment()
      val argument = Bundle()
      argument.putInt(KEY_IMAGE_RES, drawableRes)
      fragment.arguments = argument
      return fragment
    }
  }
}
