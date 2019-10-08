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

package com.google.samples.gridtopager.adapter

import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.samples.gridtopager.MainActivity
import com.google.samples.gridtopager.R
import com.google.samples.gridtopager.adapter.GridAdapter.ImageViewHolder
import com.google.samples.gridtopager.adapter.ImageData.IMAGE_DRAWABLES
import com.google.samples.gridtopager.fragment.ImagePagerFragment
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A fragment for displaying a grid of images.
 */
class GridAdapter(fragment: Fragment) : RecyclerView.Adapter<ImageViewHolder>() {

  private val requestManager: RequestManager = Glide.with(fragment)
  private val viewHolderListener: ViewHolderListener = ViewHolderListenerImpl(fragment)

  /**
   * A listener that is attached to all ViewHolders to handle image loading events and clicks.
   */
  interface ViewHolderListener {

    fun onLoadCompleted(view: ImageView, adapterPosition: Int)

    fun onItemClicked(view: View, adapterPosition: Int)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.image_card, parent, false)
    return ImageViewHolder(view, requestManager, viewHolderListener)
  }

  override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
    holder.onBind()
  }

  override fun getItemCount(): Int {
    return IMAGE_DRAWABLES.size
  }

  /**
   * Default [ViewHolderListener] implementation.
   */
  private class ViewHolderListenerImpl internal constructor(private val fragment: Fragment) : ViewHolderListener {
    private val enterTransitionStarted: AtomicBoolean = AtomicBoolean()

    override fun onLoadCompleted(view: ImageView, adapterPosition: Int) {
      // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
      if (MainActivity.currentPosition != adapterPosition) {
        return
      }
      if (enterTransitionStarted.getAndSet(true)) {
        return
      }
      fragment.startPostponedEnterTransition()
    }

    /**
     * Handles a view click by setting the current position to the given `position` and
     * starting a [ImagePagerFragment] which displays the image at the position.
     *
     * @param view the clicked [ImageView] (the shared element view will be re-mapped at the
     * GridFragment's SharedElementCallback)
     * @param adapterPosition the selected view position
     */
    override fun onItemClicked(view: View, adapterPosition: Int) {
      // Update the position.
      MainActivity.currentPosition = adapterPosition

      // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
      // instead of fading out with the rest to prevent an overlapping animation of fade and move).
      (fragment.exitTransition as TransitionSet).excludeTarget(view, true)

      val transitioningView = view.findViewById<ImageView>(R.id.card_image)
      fragment.fragmentManager!!
          .beginTransaction()
          .setReorderingAllowed(true) // Optimize for shared element transition
          .addSharedElement(transitioningView, transitioningView.transitionName)
          .replace(R.id.fragment_container, ImagePagerFragment(), ImagePagerFragment::class.java
              .simpleName)
          .addToBackStack(null)
          .commit()
    }
  }

  /**
   * ViewHolder for the grid's images.
   */
  class ImageViewHolder(
      itemView: View,
      private val requestManager: RequestManager,
      val viewHolderListener: ViewHolderListener
  ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val image: ImageView = itemView.findViewById(R.id.card_image)

    init {
      itemView.findViewById<View>(R.id.card_view).setOnClickListener(this)
    }

    /**
     * Binds this view holder to the given adapter position.
     *
     * The binding will load the image into the image view, as well as set its transition name for
     * later.
     */
    fun onBind() {
      val adapterPosition = adapterPosition
      setImage(adapterPosition)
      // Set the string value of the image resource as the unique transition name for the view.
      image.transitionName = IMAGE_DRAWABLES[adapterPosition].toString()
    }

    private fun setImage(adapterPosition: Int) {
      // Load the image with Glide to prevent OOM error when the image drawables are very large.
      requestManager
          .load(IMAGE_DRAWABLES[adapterPosition])
          .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any,
                                      target: Target<Drawable>, isFirstResource: Boolean): Boolean {
              viewHolderListener.onLoadCompleted(image, adapterPosition)
              return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
              viewHolderListener.onLoadCompleted(image, adapterPosition)
              return false
            }
          })
          .into(image)
    }

    override fun onClick(view: View) {
      // Let the listener start the ImagePagerFragment.
      viewHolderListener.onItemClicked(view, adapterPosition)
    }
  }
}