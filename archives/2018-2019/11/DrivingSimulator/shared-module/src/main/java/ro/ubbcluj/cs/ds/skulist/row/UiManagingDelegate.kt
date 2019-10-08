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
package ro.ubbcluj.cs.ds.skulist.row

import android.widget.Toast
import com.android.billingclient.api.BillingClient.SkuType
import ro.ubbcluj.cs.ds.R
import ro.ubbcluj.cs.ds.billing.BillingProvider

/**
 * Implementations of this abstract class are responsible to render UI and handle user actions for
 * skulist rows to render RecyclerView with AcquireFragment's specific UI
 */
abstract class UiManagingDelegate(protected val mBillingProvider: BillingProvider) {

  @get:SkuType
  abstract val type: String

  open fun onBindViewHolder(data: SkuRowData, holder: RowViewHolder) {
    holder.description?.text = data.description
    holder.price?.text = data.price
    holder.button!!.isEnabled = true
  }

  open fun onButtonClicked(data: SkuRowData) {
    mBillingProvider.billingManager.initiatePurchaseFlow(data.sku,
        data.skuType)
  }

  protected fun showAlreadyPurchasedToast() {
    Toast.makeText(mBillingProvider.billingManager.context,
        R.string.alert_already_purchased, Toast.LENGTH_SHORT).show()
  }
}
