/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.basictransition

import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Scene
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup

class BasicTransitionFragment : Fragment(), RadioGroup.OnCheckedChangeListener {

  // We transition between these Scenes
  private var mScene1: Scene? = null
  private var mScene2: Scene? = null
  private var mScene3: Scene? = null

  /** A custom TransitionManager  */
  private var mTransitionManagerForScene3: TransitionManager? = null

  /** Transitions take place in this ViewGroup. We retain this for the dynamic transition on scene 4.  */
  private var mSceneRoot: ViewGroup? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_basic_transition, container, false)!!
    val radioGroup = view.findViewById<View>(R.id.select_scene) as RadioGroup
    radioGroup.setOnCheckedChangeListener(this)
    mSceneRoot = view.findViewById<View>(R.id.scene_root) as ViewGroup

    // A Scene can be instantiated from a live view hierarchy.
    mScene1 = Scene(mSceneRoot, mSceneRoot!!.findViewById<View>(R.id.container) as View)

    // You can also inflate a generate a Scene from a layout resource file.
    mScene2 = Scene.getSceneForLayout(mSceneRoot, R.layout.scene2, activity)

    // Another scene from a layout resource file.
    mScene3 = Scene.getSceneForLayout(mSceneRoot, R.layout.scene3, activity)

    // We create a custom TransitionManager for Scene 3, in which ChangeBounds and Fade
    // take place at the same time.
    mTransitionManagerForScene3 = TransitionInflater.from(activity)
        .inflateTransitionManager(R.transition.scene3_transition_manager, mSceneRoot)

    return view
  }

  override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
    when (checkedId) {
      R.id.select_scene_1 -> {
        // You can start an automatic transition with TransitionManager.go().
        TransitionManager.go(mScene1)
      }
      R.id.select_scene_2 -> {
        TransitionManager.go(mScene2)
      }
      R.id.select_scene_3 -> {
        // You can also start a transition with a custom TransitionManager.
        mTransitionManagerForScene3!!.transitionTo(mScene3)
      }
      R.id.select_scene_4 -> {
        // Alternatively, transition can be invoked dynamically without a Scene.
        // For this, we first call TransitionManager.beginDelayedTransition().
        TransitionManager.beginDelayedTransition(mSceneRoot)
        // Then, we can just change view properties as usual.
        val square = mSceneRoot!!.findViewById<View>(R.id.transition_square)
        val params = square.layoutParams
        val newSize = resources.getDimensionPixelSize(R.dimen.square_size_expanded)
        params.width = newSize
        params.height = newSize
        square.layoutParams = params
      }
    }
  }

  companion object {

    fun newInstance(): BasicTransitionFragment {
      return BasicTransitionFragment()
    }
  }

}
