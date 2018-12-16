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

import java.util.ArrayList

/**
 * Handles Ui specific to "monthly gas" - subscription row
 */
class GoldMonthlyDelegate internal constructor(billingProvider: BillingProvider) : UiManagingDelegate(billingProvider) {

  override val type: String
    @SkuType
    get() = SkuType.SUBS

  override fun onBindViewHolder(data: SkuRowData, holder: RowViewHolder) {
    super.onBindViewHolder(data, holder)
    if (mBillingProvider.isGoldMonthlySubscribed) {
      holder.button!!.setText(R.string.button_own)
    } else {
      val textId = if (mBillingProvider.isGoldYearlySubscribed)
        R.string.button_change
      else
        R.string.button_buy
      holder.button!!.setText(textId)
    }
    holder.skuIcon?.setImageResource(R.drawable.gold_icon)
  }

  override fun onButtonClicked(data: SkuRowData) {
    if (mBillingProvider.isGoldYearlySubscribed) {
      // If we already subscribed to yearly gas, launch replace flow
      val currentSubscriptionSku = ArrayList<String>()
      currentSubscriptionSku.add(GoldYearlyDelegate.SKU_ID)
      mBillingProvider.billingManager.initiatePurchaseFlow(data.sku,
          currentSubscriptionSku, data.skuType)
    } else {
      mBillingProvider.billingManager.initiatePurchaseFlow(data.sku,
          data.skuType)
    }
  }

  companion object {
    const val SKU_ID = "gold_monthly"
  }
}

