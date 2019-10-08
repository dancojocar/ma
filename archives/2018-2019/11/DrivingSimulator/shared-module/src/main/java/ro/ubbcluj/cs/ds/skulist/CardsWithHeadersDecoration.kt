// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package ro.ubbcluj.cs.ds.skulist

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

import ro.ubbcluj.cs.ds.skulist.row.RowDataProvider

/**
 * A separator for RecyclerView that keeps the specified spaces between headers and the cards.
 */
class CardsWithHeadersDecoration internal constructor(private val mRowDataProvider: RowDataProvider, private val mHeaderGap: Int,
                                                      private val mRowGap: Int) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                              state: RecyclerView.State) {

    val position = parent.getChildAdapterPosition(view)
    val data = mRowDataProvider.getData(position)

    // We should add a space on top of every header card
    if (data.rowType == SkusAdapter.TYPE_HEADER) {
      outRect.top = mHeaderGap
    }

    // Adding a space under the last item
    if (position == parent.adapter!!.itemCount - 1) {
      outRect.bottom = mHeaderGap
    } else {
      outRect.bottom = mRowGap
    }
  }
}
