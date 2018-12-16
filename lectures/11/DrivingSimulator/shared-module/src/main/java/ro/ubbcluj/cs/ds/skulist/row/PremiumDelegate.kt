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

import com.android.billingclient.api.BillingClient.SkuType
import ro.ubbcluj.cs.ds.R
import ro.ubbcluj.cs.ds.billing.BillingProvider

/**
 * Handles Ui specific to "premium" - non-consumable in-app item row
 */
class PremiumDelegate internal constructor(billingProvider: BillingProvider) : UiManagingDelegate(billingProvider) {

  override val type: String
    @SkuType
    get() = SkuType.INAPP

  override fun onBindViewHolder(data: SkuRowData, holder: RowViewHolder) {
    super.onBindViewHolder(data, holder)
    val textId = if (mBillingProvider.isPremiumPurchased)
      R.string.button_own
    else
      R.string.button_buy
    holder.button!!.setText(textId)
    holder.skuIcon?.setImageResource(R.drawable.premium_icon)
  }

  override fun onButtonClicked(data: SkuRowData) {
    if (mBillingProvider.isPremiumPurchased) {
      showAlreadyPurchasedToast()
    } else {
      super.onButtonClicked(data)
    }
  }

  companion object {
    const val SKU_ID = "premium"
  }
}

