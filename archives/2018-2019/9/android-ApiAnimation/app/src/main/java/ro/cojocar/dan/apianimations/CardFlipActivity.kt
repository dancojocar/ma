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
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.*

/**
 * Demonstrates a "card-flip" animation using custom fragment transactions
 * ([android.app.FragmentTransaction.setCustomAnimations]).
 *
 *
 *
 * This sample shows an "info" action bar button that shows the back
 * of a "card", rotating the front of the card out and the back of the
 * card in. The reverse animation is played when the user presses the
 * system Back button or the "photo" action bar button.
 */
class CardFlipActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    /**
     * A handler object, used for deferring UI operations.
     */
    private val mHandler = Handler()

    /**
     * Whether or not we're showing the back of the card
     * (otherwise showing the front).
     */
    private var mShowingBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_flip)

        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment
            // representing the front of the card to this activity.
            // If there is saved instance state, this fragment will
            // have already been added to the activity.
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, CardFrontFragment())
                .commit()
        } else {
            mShowingBack = supportFragmentManager.backStackEntryCount > 0
        }

        // Monitor back stack changes to ensure the action bar shows
        // the appropriate button (either "photo" or "info").
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        // Add either a "photo" or "finish" button to the action bar,
        // depending on which page is currently selected.
        val item = menu.add(
            Menu.NONE, R.id.action_flip, Menu.NONE,
            if (mShowingBack) R.string.action_photo else R.string.action_info
        )
        item.setIcon(
            if (mShowingBack)
                R.drawable.ic_action_photo
            else
                R.drawable.ic_action_info
        )
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, Intent(this, AnimationsActivity::class.java))
                return true
            }

            R.id.action_flip -> {
                flipCard()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun flipCard() {
        if (mShowingBack) {
            supportFragmentManager.popBackStack()
            return
        }

        // Flip to the back.

        mShowingBack = true

        // Create and commit a new fragment transaction that adds
        // the fragment for the back of the card, uses custom animations,
        // and is part of the fragment manager's back stack.

        supportFragmentManager
            .beginTransaction()

            // Replace the default fragment animations with animator
            // resources representing rotations when switching to the
            // back of the card, as well as animator resources representing
            // rotations when flipping back to the front (e.g. when
            // the system Back button is pressed).
            .setCustomAnimations(
                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                R.animator.card_flip_left_in, R.animator.card_flip_left_out
            )

            // Replace any fragments currently in the container view with
            // a fragment representing the next page (indicated by the
            // just-incremented currentPage variable).
            .replace(R.id.container, CardBackFragment())

            // Add this transaction to the back stack, allowing users to
            // press Back to get to the front of the card.
            .addToBackStack(null)

            // Commit the transaction.
            .commit()

        // Defer an invalidation of the options menu (on modern devices,
        // the action bar). This can't be done immediately because the
        // transaction may not yet be committed. Commits are asynchronous
        // in that they are posted to the main thread's message loop.
        mHandler.post { invalidateOptionsMenu() }
    }

    override fun onBackStackChanged() {
        mShowingBack = supportFragmentManager.backStackEntryCount > 0

        // When the back stack changes, invalidate the options menu
        // (action bar).
        invalidateOptionsMenu()
    }

    /**
     * A fragment representing the front of the card.
     */
    class CardFrontFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(
                R.layout.fragment_card_front, container,
                false
            )
        }
    }

    /**
     * A fragment representing the back of the card.
     */
    class CardBackFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(
                R.layout.fragment_card_back, container,
                false
            )
        }
    }
}
