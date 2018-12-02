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

import android.app.Activity
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView


/**
 * The launchpad activity for this sample project.
 * This activity launches other activities that
 * demonstrate implementations of common animations.
 */
class AnimationsActivity : ListActivity() {
    /**
     * The collection of all samples in the app.
     * This gets instantiated in [ ][.onCreate] because the [Sample]
     * constructor needs access to [android.content.res.Resources].
     */
    private var mSamples: Array<Sample>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_main)

        // Instantiate the list of samples.
        mSamples = arrayOf(
            Sample(R.string.title_crossfade, CrossfadeActivity::class.java),
            Sample(R.string.title_card_flip, CardFlipActivity::class.java),
            Sample(R.string.title_screen_slide, ScreenSlideActivity::class.java),
            Sample(R.string.title_zoom, ZoomActivity::class.java),
            Sample(R.string.title_layout_changes, LayoutChangesActivity::class.java)
        )

        listAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            mSamples!!
        )
    }

    override fun onListItemClick(
        listView: ListView, view: View,
        position: Int, id: Long
    ) {
        // Launch the sample associated with this list position.
        startActivity(
            Intent(
                this@AnimationsActivity,
                mSamples!![position].activityClass
            )
        )
    }

    /**
     * This class describes an individual sample
     * (the sample title, and the activity class that
     * demonstrates this sample).
     */
    private inner class Sample(titleResId: Int, val activityClass: Class<out Activity>) {
        private val title: CharSequence

        init {
            this.title = resources.getString(titleResId)
        }

        override fun toString(): String {
            return title.toString()
        }
    }
}
