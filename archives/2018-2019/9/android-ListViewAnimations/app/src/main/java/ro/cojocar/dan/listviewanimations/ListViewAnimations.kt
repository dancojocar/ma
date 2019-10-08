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

package ro.cojocar.dan.listviewanimations

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_list_view_animations.*
import java.util.*

/**
 * This example shows how animating ListView items can lead to problems as views are recycled,
 * and how to perform these types of animations correctly with new API added in Jellybean.
 */
class ListViewAnimations : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_list_view_animations)

    val cheeseList = ArrayList<String>()
    for (i in Cheeses.sCheeseStrings.indices) {
      cheeseList.add(Cheeses.sCheeseStrings[i])
    }
    val adapter = StableArrayAdapter(
      this,
      android.R.layout.simple_list_item_1, cheeseList
    )
    listview.adapter = adapter

    listview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, _ ->
      val item = parent.getItemAtPosition(position) as String
      if (vpaCB.isChecked) {
        view.animate().setDuration(1000).alpha(0f).withEndAction {
          removeElement(cheeseList, item, adapter)
          view.alpha = 1f
        }
      } else {
        // Here's where the problem starts - this animation will animate a View object.
        // But that View may get recycled if it is animated out of the container,
        // and the animation will continue to fade a view that now contains unrelated
        // content.
        val anim = ObjectAnimator.ofFloat<View>(view, View.ALPHA, 0f)
        anim.duration = 1000
        if (setTransientStateCB.isChecked) {
          // Here's the correct way to do this: if you tell a view that it has
          // transientState, then ListView ill avoid recycling it until the
          // transientState flag is reset.
          // A different approach is to use ViewPropertyAnimator, which sets the
          // transientState flag internally.
          view.setHasTransientState(true)
        }
        anim.addListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            removeElement(cheeseList, item, adapter)
            view.alpha = 1f
            if (setTransientStateCB.isChecked) {
              view.setHasTransientState(false)
            }
          }
        })
        anim.start()
      }
    }
  }

  private fun removeElement(
    cheeseList: ArrayList<String>,
    item: String,
    adapter: StableArrayAdapter
  ) {
    cheeseList.remove(item)
    adapter.notifyDataSetChanged()
  }

  private inner class StableArrayAdapter(
    context: Context, textViewResourceId: Int,
    objects: List<String>
  ) : ArrayAdapter<String>(context, textViewResourceId, objects) {

    internal var mIdMap = HashMap<String, Int>()

    init {
      for (i in objects.indices) {
        mIdMap[objects[i]] = i
      }
    }

    override fun getItemId(position: Int): Long {
      val item = getItem(position)!!
      return mIdMap[item]!!.toLong()
    }

    override fun hasStableIds(): Boolean {
      return true
    }
  }
}
