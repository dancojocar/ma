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
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import ro.cojocar.dan.apianimations.databinding.ActivityCrossfadeBinding

/**
 * This sample demonstrates cross-fading between two overlapping views.
 *
 *
 *
 * In this sample, the two overlapping views are a loading indicator
 * and some text content. The active view is toggled by touching the
 * toggle button in the action bar. In real-world applications, this
 * toggle would occur as soon as content was available. Note that if
 * content is immediately available, a loading spinner shouldn't be
 * presented and there should be no animation.
 */
class CrossfadeActivity : Activity() {
  private lateinit var binding: ActivityCrossfadeBinding
  /**
   * The flag indicating whether content is loaded
   * (text is shown) or not (loading spinner is
   * shown).
   */
  private var mContentLoaded: Boolean = false

  /**
   * The view (or view group) containing the content.
   * This is one of two overlapping views.
   */
  private var mContentView: View? = null

  /**
   * The view containing the loading indicator.
   * This is the other of two overlapping views.
   */
  private var mLoadingView: View? = null

  /**
   * The system "short" animation time duration,
   * in milliseconds. This duration is ideal for
   * subtle animations or animations that occur
   * very frequently.
   */
  private var mShortAnimationDuration: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityCrossfadeBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    mContentView = binding.excerptContent.content
    mLoadingView = binding.loadingSpinner

    // Initially hide the content view.
    mContentView!!.visibility = View.GONE

    // Retrieve and cache the system's default "short" animation time.
    mShortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    super.onCreateOptionsMenu(menu)
    menuInflater.inflate(R.menu.activity_crossfade, menu)
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

      R.id.action_toggle -> {
        // Toggle whether content is loaded.
        mContentLoaded = !mContentLoaded
        showContentOrLoadingIndicator(mContentLoaded)
        return true
      }
    }

    return super.onOptionsItemSelected(item)
  }

  /**
   * Cross-fades between [.mContentView] and [.mLoadingView].
   */
  private fun showContentOrLoadingIndicator(contentLoaded: Boolean) {
    // Decide which view to hide and which to show.
    val showView = if (contentLoaded) mContentView else mLoadingView
    val hideView = if (contentLoaded) mLoadingView else mContentView

    // Set the "show" view to 0% opacity but visible,
    // so that it is visible (but fully transparent)
    // during the animation.
    showView!!.alpha = 0f
    showView.visibility = View.VISIBLE

    // Animate the "show" view to 100% opacity, and clear
    // any animation listener set on the view.
    // Remember that listeners are not limited to the specific animation
    // describes in the chained method calls. Listeners are set on the
    // ViewPropertyAnimator object for the view, which persists across
    // several animations.
    showView.animate()
      .alpha(1f)
      .setDuration(2000)
      .setListener(null)

    // Animate the "hide" view to 0% opacity. After the animation ends,
    // set its visibility to GONE as an optimization step
    // (it won't participate in layout passes, etc.)
    hideView!!.animate()
      .alpha(0f)
      .setDuration(mShortAnimationDuration.toLong())
      .setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          hideView.visibility = View.GONE
        }
      })
  }
}
