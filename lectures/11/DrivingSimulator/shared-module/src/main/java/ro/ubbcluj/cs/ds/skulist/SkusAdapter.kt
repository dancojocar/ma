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
package ro.ubbcluj.cs.ds.skulist

import java.lang.annotation.RetentionPolicy.SOURCE

import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import ro.ubbcluj.cs.ds.skulist.row.RowDataProvider
import ro.ubbcluj.cs.ds.skulist.row.RowViewHolder
import ro.ubbcluj.cs.ds.skulist.row.SkuRowData
import ro.ubbcluj.cs.ds.skulist.row.UiManager

import java.lang.annotation.Retention

/**
 * Adapter for a RecyclerView that shows SKU details for the app.
 *
 *
 * Note: It's done fragment-specific logic independent and delegates control back to the
 * specified handler (implemented inside AcquireFragment in this example)
 *
 */
class SkusAdapter : RecyclerView.Adapter<RowViewHolder>(), RowDataProvider {

  private var mUiManager: UiManager? = null
  private lateinit var mListData: List<SkuRowData>

  /**
   * Types for adapter rows
   */
  @Retention(SOURCE)
  @IntDef(TYPE_HEADER, TYPE_NORMAL)
  annotation class RowTypeDef

  internal fun setUiManager(uiManager: UiManager) {
    mUiManager = uiManager
  }

  internal fun updateData(data: List<SkuRowData>) {
    mListData = data
    notifyDataSetChanged()
  }

  @RowTypeDef
  override fun getItemViewType(position: Int): Int {
    return mListData[position].rowType
  }

  override fun onCreateViewHolder(parent: ViewGroup, @RowTypeDef viewType: Int): RowViewHolder {
    return mUiManager!!.onCreateViewHolder(parent, viewType)
  }

  override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
    mUiManager!!.onBindViewHolder(getData(position), holder)
  }

  override fun getItemCount(): Int {
    return mListData.size
  }

  override fun getData(position: Int): SkuRowData {
    return mListData[position]
  }

  companion object {
    const val TYPE_HEADER = 0
    const val TYPE_NORMAL = 1
  }
}

