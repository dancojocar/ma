/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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
package ro.ubbcluj.cs.ds.skulist.row

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import ro.ubbcluj.cs.ds.R

/**
 * ViewHolder for quick access to row's views
 */
class RowViewHolder(itemView: View, clickListener: OnButtonClickListener) : RecyclerView.ViewHolder(itemView) {
  var title: TextView?
  var description: TextView?
  var price: TextView?
  var button: Button? = null
  var skuIcon: ImageView?

  /**
   * Handler for a button click on particular row
   */
  interface OnButtonClickListener {
    fun onButtonClicked(position: Int)
  }

  init {
    title = itemView.findViewById(R.id.title)
    price = itemView.findViewById(R.id.price)
    description = itemView.findViewById(R.id.description)
    skuIcon = itemView.findViewById(R.id.sku_icon)
    button = itemView.findViewById(R.id.state_button)
    if (button != null) {
      button!!.setOnClickListener { clickListener.onButtonClicked(adapterPosition) }
    }
  }
}
