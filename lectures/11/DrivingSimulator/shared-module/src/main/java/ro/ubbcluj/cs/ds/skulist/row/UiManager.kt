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


import android.view.LayoutInflater
import android.view.ViewGroup
import ro.ubbcluj.cs.ds.R
import ro.ubbcluj.cs.ds.billing.BillingProvider
import ro.ubbcluj.cs.ds.skulist.SkusAdapter
import ro.ubbcluj.cs.ds.skulist.SkusAdapter.RowTypeDef

/**
 * Renders the UI for a particular row by delegating specifics to corresponding handlers
 */
class UiManager(private val mRowDataProvider: RowDataProvider, billingProvider: BillingProvider) : RowViewHolder.OnButtonClickListener {
  val delegatesFactory: UiDelegatesFactory = UiDelegatesFactory(billingProvider)

  fun onCreateViewHolder(parent: ViewGroup, @RowTypeDef viewType: Int): RowViewHolder {
    // Selecting a flat layout for header rows
    return if (viewType == SkusAdapter.TYPE_HEADER) {
      val item = LayoutInflater.from(parent.context)
          .inflate(R.layout.sku_details_row_header, parent, false)
      RowViewHolder(item, this)
    } else {
      val item = LayoutInflater.from(parent.context)
          .inflate(R.layout.sku_details_row, parent, false)
      RowViewHolder(item, this)
    }
  }

  fun onBindViewHolder(data: SkuRowData?, holder: RowViewHolder) {
    if (data != null) {
      holder.title?.text = data.title
      // For non-header rows we need to feel other data and init button's state
      if (data.rowType != SkusAdapter.TYPE_HEADER) {
        delegatesFactory.onBindViewHolder(data, holder)
      }
    }
  }

  override fun onButtonClicked(position: Int) {
    val data = mRowDataProvider.getData(position)
    delegatesFactory.onButtonClicked(data)
  }
}
