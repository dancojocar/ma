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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A fragment representing a single step in a wizard.
 * The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 *
 *
 * This class is used by the [CardFlipActivity]
 * and [ScreenSlideActivity] samples.
 */
class ScreenSlidePageFragment : Fragment() {

    /**
     * The fragment's page number, which is set to the argument
     * value for [.ARG_PAGE].
     */
    private var mPageNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPageNumber = arguments!!.getInt(ARG_PAGE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout containing a title and body text.
        val rootView = inflater
            .inflate(
                R.layout.fragment_screen_slide_page, container,
                false
            ) as ViewGroup

        // Set the title view to show the page number.
        val tv = rootView.findViewById<TextView>(android.R.id.text1)
        tv.text = getString(R.string.title_template_step, mPageNumber + 1)

        return rootView
    }

    companion object {
        /**
         * The argument key for the page number this fragment represents.
         */
        const val ARG_PAGE = "page"

        /**
         * Factory method for this fragment class. Constructs a new
         * fragment for the given page number.
         */
        fun create(pageNumber: Int): ScreenSlidePageFragment {
            val fragment = ScreenSlidePageFragment()
            val args = Bundle()
            args.putInt(ARG_PAGE, pageNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
