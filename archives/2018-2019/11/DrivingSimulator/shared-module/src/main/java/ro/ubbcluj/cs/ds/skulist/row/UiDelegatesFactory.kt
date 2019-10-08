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
import ro.ubbcluj.cs.ds.billing.BillingProvider
import java.util.ArrayList
import java.util.HashMap

/**
 * This factory is responsible to finding the appropriate delegate for Ui rendering and calling
 * corresponding method on it.
 */
class UiDelegatesFactory(provider: BillingProvider) {
  private val uiDelegates: MutableMap<String, UiManagingDelegate>

  init {
    uiDelegates = HashMap()
    uiDelegates[GasDelegate.SKU_ID] = GasDelegate(provider)
    uiDelegates[GoldMonthlyDelegate.SKU_ID] = GoldMonthlyDelegate(provider)
    uiDelegates[GoldYearlyDelegate.SKU_ID] = GoldYearlyDelegate(provider)
    uiDelegates[PremiumDelegate.SKU_ID] = PremiumDelegate(provider)
  }

  /**
   * Returns the list of all SKUs for the billing type specified
   */
  fun getSkuList(@SkuType billingType: String): List<String> {
    val result = ArrayList<String>()
    for (skuId in uiDelegates.keys) {
      val delegate = uiDelegates[skuId]
      if (delegate!!.type == billingType) {
        result.add(skuId)
      }
    }
    return result
  }

  fun onBindViewHolder(data: SkuRowData, holder: RowViewHolder) {
    uiDelegates[data.sku]!!.onBindViewHolder(data, holder)
  }

  fun onButtonClicked(data: SkuRowData) {
    uiDelegates[data.sku]!!.onButtonClicked(data)
  }
}
