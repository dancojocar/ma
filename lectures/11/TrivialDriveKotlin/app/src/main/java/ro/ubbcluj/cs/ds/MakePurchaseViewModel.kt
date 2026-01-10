/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.ubbcluj.cs.ds

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.HashMap
import kotlin.collections.set

/*
   This is used for any business logic, as well as to echo LiveData from the BillingRepository.
*/
class MakePurchaseViewModel(private val tdr: TrivialDriveRepository) : ViewModel() {

    companion object {
        val TAG = "TrivialDrive:" + MakePurchaseViewModel::class.java.simpleName
        private val skuToResourceIdMap: MutableMap<String, Int> = HashMap()

        init {
            skuToResourceIdMap[TrivialDriveRepository.SKU_GAS] = R.drawable.buy_gas
            skuToResourceIdMap[TrivialDriveRepository.SKU_PREMIUM] = R.drawable.upgrade_app
            skuToResourceIdMap[TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY] = R.drawable.get_infinite_gas
            skuToResourceIdMap[TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY] = R.drawable.get_infinite_gas
        }
    }

    class SkuDetails internal constructor(val sku: String, tdr: TrivialDriveRepository) {
        val title = tdr.getSkuTitle(sku).asLiveData()
        val description = tdr.getSkuDescription(sku).asLiveData()
        val price = tdr.getSkuPrice(sku).asLiveData()
        val iconDrawableId = skuToResourceIdMap[sku]!!
    }

    fun getSkuDetails(sku: String): SkuDetails {
        return SkuDetails(sku, tdr)
    }

    fun canBuySku(sku: String): LiveData<Boolean> {
        return tdr.canPurchase(sku).asLiveData()
    }

    fun isPurchased(sku: String): LiveData<Boolean> {
        return tdr.isPurchased(sku).asLiveData()
    }

    /**
     * Starts a billing flow for purchasing gas.
     * @param activity
     * @return whether or not we were able to start the flow
     */
    fun buySku(activity: Activity, sku: String) {
        tdr.buySku(activity, sku)
    }

    val billingFlowInProcess: LiveData<Boolean>
        get() = tdr.billingFlowInProcess.asLiveData()

    fun sendMessage(message: Int) {
        viewModelScope.launch {
            tdr.sendMessage(message)
        }
    }

    fun consumePremium() {
        tdr.debugConsumePremium()
    }

    class MakePurchaseViewModelFactory(private val trivialDriveRepository: TrivialDriveRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MakePurchaseViewModel::class.java)) {
                return MakePurchaseViewModel(trivialDriveRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
