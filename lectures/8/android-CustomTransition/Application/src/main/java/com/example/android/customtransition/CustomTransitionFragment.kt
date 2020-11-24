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

package com.example.android.customtransition

import android.os.Bundle
import android.transition.Scene
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class CustomTransitionFragment : Fragment(), View.OnClickListener {

  /** These are the Scenes we use.  */
  private var mScenes: Array<Scene>? = null

  /** The current index for mScenes.  */
  private var mCurrentScene: Int = 0

  /** This is the custom Transition we use in this sample.  */
  private var mTransition: Transition? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_custom_transition, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val context = activity
    val container = view.findViewById<View>(R.id.container) as FrameLayout
    view.findViewById<View>(R.id.show_next_scene).setOnClickListener(this)
    if (null != savedInstanceState) {
      mCurrentScene = savedInstanceState.getInt(STATE_CURRENT_SCENE)
    }
    // We set up the Scenes here.
    mScenes = arrayOf(
        Scene.getSceneForLayout(container, R.layout.scene1, context),
        Scene.getSceneForLayout(container, R.layout.scene2, context),
        Scene.getSceneForLayout(container, R.layout.scene3, context))
    // This is the custom Transition.
    mTransition = ChangeColor()
    // Show the initial Scene.
    TransitionManager.go(mScenes!![mCurrentScene % mScenes!!.size])
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(STATE_CURRENT_SCENE, mCurrentScene)
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.show_next_scene -> {
        mCurrentScene = (mCurrentScene + 1) % mScenes!!.size
        // Pass the custom Transition as second argument for TransitionManager.go
        TransitionManager.go(mScenes!![mCurrentScene], mTransition)
      }
    }
  }

  companion object {
    private const val STATE_CURRENT_SCENE = "current_scene"
  }
}
