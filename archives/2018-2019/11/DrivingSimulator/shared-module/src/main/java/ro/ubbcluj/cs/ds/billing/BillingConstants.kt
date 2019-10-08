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
package ro.ubbcluj.cs.ds.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.SkuType
import java.util.Arrays

/**
 * Static fields and methods useful for billing
 */
object BillingConstants {
  // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
  const val SKU_PREMIUM = "premium"
  const val SKU_GAS = "gas"

  // SKU for our subscription (infinite gas)
  const val SKU_GOLD_MONTHLY = "gold_monthly"
  const val SKU_GOLD_YEARLY = "gold_yearly"

  private val IN_APP_SKUS = arrayOf(SKU_GAS, SKU_PREMIUM)
  private val SUBSCRIPTIONS_SKUS = arrayOf(SKU_GOLD_MONTHLY, SKU_GOLD_YEARLY)

  /**
   * Returns the list of all SKUs for the billing type specified
   */
  fun getSkuList(@BillingClient.SkuType billingType: String): List<String> {
    return if (billingType === SkuType.INAPP)
      Arrays.asList(*IN_APP_SKUS)
    else
      Arrays.asList(*SUBSCRIPTIONS_SKUS)
  }
}

