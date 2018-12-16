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
import com.android.billingclient.api.SkuDetails
import ro.ubbcluj.cs.ds.skulist.SkusAdapter
import ro.ubbcluj.cs.ds.skulist.SkusAdapter.RowTypeDef

/**
 * A model for SkusAdapter's row
 */
class SkuRowData {
  var sku: String = ""
  var title: String? = null
    private set
  var price: String = ""
  var description: String = ""
  @RowTypeDef
  @get:RowTypeDef
  var rowType: Int = 0
    private set
  @SkuType
  @get:SkuType
  var skuType: String = ""

  constructor(details: SkuDetails, @RowTypeDef rowType: Int,
              @SkuType billingType: String) {
    this.sku = details.sku
    this.title = details.title
    this.price = details.price
    this.description = details.description
    this.rowType = rowType
    this.skuType = billingType
  }

  constructor(title: String) {
    this.title = title
    this.rowType = SkusAdapter.TYPE_HEADER
  }
}
