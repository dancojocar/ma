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

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.app.NavUtils
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

/**
 * Demonstrates a "screen-slide" animation using a [ViewPager].
 * Because [ViewPager] automatically plays such an animation
 * when calling [ViewPager.setCurrentItem], there
 * isn't any animation-specific code in this sample.
 *
 *
 *
 * This sample shows a "next" button that advances the user to the
 * next step in a wizard, animating the current screen out (to the left)
 * and the next screen in (from the right). The reverse animation is
 * played when the user presses the "previous" button.
 *
 * @see ScreenSlidePageFragment
 */
class ScreenSlideActivity : AppCompatActivity() {

    /**
     * The pager widget, which handles animation and allows swiping
     * horizontally to access previous and next wizard steps.
     */
    private var mPager: ViewPager? = null

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private var mPagerAdapter: PagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager)
        mPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager!!.adapter = mPagerAdapter
        mPager!!.addOnPageChangeListener(
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    // When changing pages, reset the action bar actions since
                    // they are dependent on which page is currently active.
                    // An alternative approach is to have each fragment expose
                    // actions itself (rather than the activity exposing actions),
                    // but for simplicity, the activity provides the actions in
                    // this sample.
                    invalidateOptionsMenu()
                }
            })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_screen_slide, menu)

        menu.findItem(R.id.action_previous).isEnabled = mPager!!.currentItem > 0

        // Add either a "next" or "finish" button to the action bar,
        // depending on which page is currently selected.
        val item = menu.add(
            Menu.NONE, R.id.action_next, Menu.NONE,
            if (mPager!!.currentItem == mPagerAdapter!!.count - 1)
                R.string.action_finish
            else
                R.string.action_next
        )
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(
                    this,
                    Intent(this, MainActivity::class.java)
                )
                return true
            }

            R.id.action_previous -> {
                // Go to the previous step in the wizard.
                // If there is no previous step,
                // setCurrentItem will do nothing.
                mPager!!.currentItem = mPager!!.currentItem - 1
                return true
            }

            R.id.action_next -> {
                // Advance to the next step in the wizard.
                // If there is no next step, setCurrentItem
                // will do nothing.
                mPager!!.currentItem = mPager!!.currentItem + 1
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * A simple pager adapter that represents 5
     * [ScreenSlidePageFragment] objects, in sequence.
     */
    private inner class ScreenSlidePagerAdapter internal constructor(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {


        override fun getItem(position: Int): Fragment {
            return ScreenSlidePageFragment.create(position)
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    companion object {
        /**
         * The number of pages (wizard steps) to show in this demo.
         */
        private const val NUM_PAGES = 5
    }
}
