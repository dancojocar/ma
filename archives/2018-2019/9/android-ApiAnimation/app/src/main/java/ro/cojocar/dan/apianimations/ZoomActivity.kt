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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NavUtils
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

/**
 * A sample showing how to zoom an image thumbnail to full-screen,
 * by animating the bounds of the zoomed image from the thumbnail
 * bounds to the screen bounds.
 *
 *
 *
 * In this sample, the user can touch one of two images.
 * Touching an image zooms it in, covering the entire activity
 * content area. Touching the zoomed-in image hides it.
 */
class ZoomActivity : FragmentActivity() {
    /**
     * Hold a reference to the current animator, so that it
     * can be canceled mid-way.
     */
    private var mCurrentAnimator: Animator? = null

    /**
     * The system "short" animation time duration, in milliseconds.
     * This duration is ideal for subtle animations or animations
     * that occur very frequently.
     */
    private var animationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)

        // Hook up clicks on the thumbnail views.

        val thumb1View = findViewById<View>(R.id.thumb_button_1)
        thumb1View.setOnClickListener { zoomImageFromThumb(thumb1View, R.drawable.image1) }

        val thumb2View = findViewById<View>(R.id.thumb_button_2)
        thumb2View.setOnClickListener { zoomImageFromThumb(thumb2View, R.drawable.image2) }

        // Retrieve and cache the system's default "short" animation time.
        animationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
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
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * "Zooms" in a thumbnail view by assigning the high resolution
     * image to a hidden "zoomed-in" image view and animating its
     * bounds to fit the entire activity content area. More specifically:
     *
     *
     *
     *  1. Assign the high-res image to the hidden "zoomed-in" (expanded)
     * image view.
     *  1. Calculate the starting and ending bounds for the expanded view.
     *  1. Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
     * simultaneously, from the starting bounds to the ending bounds.
     *  1. Zoom back out by running the reverse animation on click.
     *
     *
     * @param thumbView  The thumbnail view to zoom in.
     * @param imageResId The high-resolution version of the image
     * represented by the thumbnail.
     */
    private fun zoomImageFromThumb(thumbView: View, imageResId: Int) {
        // If there's an animation in progress, cancel it immediately
        // and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator!!.cancel()
        }

        // Load the high-resolution "zoomed-in" image.
        val expandedImageView = findViewById<ImageView>(R.id.expanded_image)
        expandedImageView.setImageResource(imageResId)

        // Calculate the starting and ending bounds for the
        // zoomed-in image. This step involves some math.
        val startBounds = Rect()
        val finalBounds = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of
        // the thumbnail, and the final bounds are the global visible
        // rectangle of the container view. Also set the container
        // view's offset as the origin for the bounds, since that's
        // the origin for the positioning animation properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds)
        findViewById<View>(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset)
        startBounds.offset(-globalOffset.x, -globalOffset.y)
        finalBounds.offset(-globalOffset.x, -globalOffset.y)

        // Adjust the start bounds to be the same aspect ratio as
        // the final bounds using the "center crop" technique.
        // This prevents undesirable stretching during the animation.
        // Also calculate the start scaling factor (the end scaling
        // factor is always 1.0).
        val startScale: Float
        if (finalBounds.width().toFloat() / finalBounds.height() > startBounds.width().toFloat() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = startBounds.height().toFloat() / finalBounds.height()
            val startWidth = startScale * finalBounds.width()
            val deltaWidth = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width().toFloat() / finalBounds.width()
            val startHeight = startScale * finalBounds.height()
            val deltaHeight = (startHeight - startBounds.height()) / 2
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view.
        // When the animation begins, it will position the
        // zoomed-in view in the place of the thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default is
        // the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four
        // translation and scale properties (X, Y, SCALE_X, and SCALE_Y).
        val set = AnimatorSet()
        set.play(
            ObjectAnimator.ofFloat<View>(
                expandedImageView,
                View.X,
                startBounds.left.toFloat(),
                finalBounds.left.toFloat()
            )
        )
            .with(
                ObjectAnimator.ofFloat<View>(
                    expandedImageView, View.Y,
                    startBounds.top.toFloat(), finalBounds.top.toFloat()
                )
            )
            .with(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.SCALE_X, startScale, 1f
                )
            )
            .with(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.SCALE_Y, startScale, 1f
                )
            )
        set.duration = animationDuration.toLong()
        set.interpolator = DecelerateInterpolator()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mCurrentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                mCurrentAnimator = null
            }
        })
        set.start()
        mCurrentAnimator = set

        // Upon clicking the zoomed-in image, it should zoom back
        // down to the original bounds and show the thumbnail instead
        // of the expanded image.
        expandedImageView.setOnClickListener {
            if (mCurrentAnimator != null) {
                mCurrentAnimator!!.cancel()
            }

            // Animate the four positioning/sizing properties
            // in parallel, back to their original values.
            val aSet = AnimatorSet()
            aSet
                .play(
                    ObjectAnimator.ofFloat<View>(
                        expandedImageView,
                        View.X, startBounds.left.toFloat()
                    )
                )
                .with(
                    ObjectAnimator.ofFloat<View>(
                        expandedImageView,
                        View.Y, startBounds.top.toFloat()
                    )
                )
                .with(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.SCALE_X, startScale
                    )
                )
                .with(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.SCALE_Y, startScale
                    )
                )
            aSet.duration = animationDuration.toLong()
            aSet.interpolator = DecelerateInterpolator()
            aSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                }
            })
            aSet.start()
            mCurrentAnimator = aSet
        }
    }
}
