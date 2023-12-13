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
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import ro.ubbcluj.cs.ds.billing.BillingDataSource
import ro.ubbcluj.cs.ds.db.GameStateModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The repository uses data from the Billing data source and the game state model together to give
 * a unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
class TrivialDriveRepository(
    private val billingDataSource: BillingDataSource,
    private val gameStateModel: GameStateModel,
    private val defaultScope: CoroutineScope
) {
    private val gameMessages: MutableSharedFlow<Int> = MutableSharedFlow()

    /**
     * Sets up the event that we can use to send messages up to the UI to be used in Snackbars.
     * This collects new purchase events from the BillingDataSource, transforming the known SKU
     * strings into useful String messages, and emitting the messages into the game messages flow.
     */
    private fun postMessagesFromBillingFlow() {
        defaultScope.launch {
            try {
                billingDataSource.getNewPurchases().collect { skuList ->
                    // TODO: Handle multi-line purchases better
                    for ( sku in skuList ) {
                        when (sku) {
                            SKU_GAS -> gameMessages.emit(R.string.message_more_gas_acquired)
                            SKU_PREMIUM -> gameMessages.emit(R.string.message_premium)
                            SKU_INFINITE_GAS_MONTHLY,
                            SKU_INFINITE_GAS_YEARLY -> {
                                // this makes sure that upgrades/downgrades to subscriptions are
                                // reflected correctly in our user interface
                                billingDataSource.refreshPurchases()
                                gameMessages.emit(R.string.message_subscribed)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Collection complete")
            }
            Log.d(TAG, "Collection Coroutine Scope Exited")
        }
    }

    /**
     * Uses one unit of gas if we don't have a subscription.
     */
    suspend fun drive() {
        when (val gasTankLevel = gasTankLevel().first()) {
            GAS_TANK_INFINITE -> sendMessage(R.string.message_infinite_drive)
            GAS_TANK_MIN -> sendMessage(R.string.message_out_of_gas)
            else -> {
                val newGasLevel = gasTankLevel - gameStateModel.decrementGas(GAS_TANK_MIN)
                Log.d(TAG, "Old Gas Level: $gasTankLevel New Gas Level: $newGasLevel")
                if (newGasLevel == GAS_TANK_MIN) {
                    sendMessage(R.string.message_out_of_gas)
                } else {
                    sendMessage(R.string.message_you_drove)
                }
            }
        }
    }

    /**
     * Automatic support for upgrading/downgrading subscription.
     * @param activity
     * @param sku
     */
    fun buySku(activity: Activity, sku: String) {
        var oldSku: String? = null
        when (sku) {
            SKU_INFINITE_GAS_MONTHLY -> oldSku = SKU_INFINITE_GAS_YEARLY
            SKU_INFINITE_GAS_YEARLY -> oldSku = SKU_INFINITE_GAS_MONTHLY
        }
        if (oldSku == null) {
            billingDataSource.launchBillingFlow(activity, sku)
        } else {
            billingDataSource.launchBillingFlow(activity, sku, oldSku)
        }
    }

    /**
     * Return Flow that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow that returns true if the sku is purchased.
     */
    fun isPurchased(sku: String): Flow<Boolean> {
        return billingDataSource.isPurchased(sku)
    }

    /**
     * We can buy gas if:
     * 1) We can add at least one unit of gas
     * 2) The billing data source allows us to purchase, which means that the item isn't already
     *    purchased.
     * For other skus, we rely on just the data from the billing data source. For subscriptions,
     * only one can be held at a time, and purchasing one subscription will use the billing feature
     * to upgrade or downgrade the user from the other.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow<Boolean> that returns true if the sku can be purchased
     */
    fun canPurchase(sku: String): Flow<Boolean> {
        return when (sku) {
            SKU_GAS -> {
                billingDataSource.canPurchase(sku).combine(gasTankLevel()) { canPurchase, gasTankLevel ->
                    canPurchase && gasTankLevel < GAS_TANK_MAX
                }
            }
            else -> billingDataSource.canPurchase(sku)
        }
    }

    /**
     * Combine the results from our subscription flow with our gas tank level from the game state
     * database to get our displayed gas tank level, which will be infinite if a subscription is
     * active.
     *
     * @return Flow that represents the gasTankLevel by game logic.
     */
    fun gasTankLevel(): Flow<Int> {
        val gasTankLevelFlow = gameStateModel.gasTankLevel()
        val monthlySubPurchasedFlow = isPurchased(SKU_INFINITE_GAS_MONTHLY)
        val yearlySubPurchasedFlow = isPurchased(SKU_INFINITE_GAS_YEARLY)
        return combine(
            gasTankLevelFlow,
            monthlySubPurchasedFlow,
            yearlySubPurchasedFlow
        ) { gasTankLevel, monthlySubPurchased, yearlySubPurchased ->
            when {
                monthlySubPurchased || yearlySubPurchased -> GAS_TANK_INFINITE
                else -> gasTankLevel
            }
        }
    }

    suspend fun refreshPurchases() {
        billingDataSource.refreshPurchases()
    }

    val billingLifecycleObserver: LifecycleObserver
        get() = billingDataSource

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    fun getSkuTitle(sku: String): Flow<String> {
        return billingDataSource.getSkuTitle(sku)
    }

    fun getSkuPrice(sku: String): Flow<String> {
        return billingDataSource.getSkuPrice(sku)
    }

    fun getSkuDescription(sku: String): Flow<String> {
        return billingDataSource.getSkuDescription(sku)
    }

    val messages: Flow<Int>
        get() = gameMessages

    suspend fun sendMessage(stringId: Int) {
        gameMessages.emit(stringId)
    }

    val billingFlowInProcess: Flow<Boolean>
        get() = billingDataSource.getBillingFlowInProcess()

    fun debugConsumePremium() {
        CoroutineScope(Dispatchers.Main).launch {
            billingDataSource.consumeInappPurchase(SKU_PREMIUM)
        }
    }

    companion object {
        // Source for all constants
        const val GAS_TANK_MIN = 0
        const val GAS_TANK_MAX = 4
        const val GAS_TANK_INFINITE = 5

        // The following SKU strings must match the ones we have in the Google Play developer console.
        // SKUs for non-subscription purchases
        const val SKU_PREMIUM = "premium"
        const val SKU_GAS = "gas"

        // SKU for subscription purchases (infinite gas)
        const val SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly"
        const val SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly"
        val TAG = TrivialDriveRepository::class.simpleName
        val INAPP_SKUS = arrayOf(SKU_PREMIUM, SKU_GAS)
        val SUBSCRIPTION_SKUS = arrayOf(
            SKU_INFINITE_GAS_MONTHLY,
            SKU_INFINITE_GAS_YEARLY
        )
        val AUTO_CONSUME_SKUS = arrayOf(SKU_GAS)
    }

    init {
        postMessagesFromBillingFlow()

        // Since both are tied to application lifecycle, we can launch this scope to collect
        // consumed purchases from the billing data source while the app process is alive.
        defaultScope.launch {
            billingDataSource.getConsumedPurchases().collect {
                for( sku in it ) {
                    if (sku == SKU_GAS) {
                        gameStateModel.incrementGas(GAS_TANK_MAX)
                    }
                }
            }
        }
    }
}
