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

package ro.ubbcluj.cs.ds

import android.content.Context.MODE_PRIVATE

import android.support.annotation.DrawableRes
import android.util.Log
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.Purchase
import ro.ubbcluj.cs.ds.billing.BillingManager.BillingUpdatesListener
import ro.ubbcluj.cs.ds.skulist.row.GasDelegate
import ro.ubbcluj.cs.ds.skulist.row.GoldMonthlyDelegate
import ro.ubbcluj.cs.ds.skulist.row.GoldYearlyDelegate
import ro.ubbcluj.cs.ds.skulist.row.PremiumDelegate

/**
 * Handles control logic of the BaseGamePlayActivity
 */
internal class MainViewController(private val mActivity: BaseGamePlayActivity) {

  val updateListener: UpdateListener

  // Tracks if we currently own subscriptions SKUs
  var isGoldMonthlySubscribed: Boolean = false
    private set
  var isGoldYearlySubscribed: Boolean = false
    private set

  // Tracks if we currently own a premium car
  var isPremiumPurchased: Boolean = false
    private set

  // Current amount of gas in tank, in units
  private var mTank: Int = 0

  val isTankEmpty: Boolean
    get() = mTank <= 0

  val isTankFull: Boolean
    get() = mTank >= TANK_MAX

  val tankResId: Int
    @DrawableRes get() {
      val index = if (mTank >= TANK_RES_IDS.size) TANK_RES_IDS.size - 1 else mTank
      return TANK_RES_IDS[index]
    }

  init {
    updateListener = UpdateListener()
    loadData()
  }

  fun useGas() {
    if (isPremiumPurchased || isGoldMonthlySubscribed || isGoldYearlySubscribed) {
      mTank = TANK_MAX
    } else {
      mTank--
      saveData()
    }
    Log.d(TAG, "Tank is now: $mTank")
  }

  /**
   * Handler to billing updates
   */
  inner class UpdateListener : BillingUpdatesListener {
    override fun onBillingClientSetupFinished() {
      mActivity.onBillingManagerSetupFinished()
    }

    override fun onConsumeFinished(token: String, @BillingResponse result: Int) {
      Log.d(TAG, "Consumption finished. Purchase token: $token, result: $result")

      // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
      // check if token corresponding to the expected sku was consumed.
      // If you have more than one sku, you probably need to validate that the token matches
      // the SKU you expect.
      // It could be done by maintaining a map (updating it every time you call consumeAsync)
      // of all tokens into SKUs which were scheduled to be consumed and then looking through
      // it here to check which SKU corresponds to a consumed token.
      if (result == BillingResponse.OK) {
        // Successfully consumed, so we apply the effects of the item in our
        // game world's logic, which in our case means filling the gas tank a bit
        Log.d(TAG, "Consumption successful. Provisioning.")
        mTank = if (mTank == TANK_MAX) TANK_MAX else mTank + 1
        saveData()
        mActivity.alert(R.string.alert_fill_gas, mTank)
      } else {
        mActivity.alert(R.string.alert_error_consuming, result)
      }

      mActivity.showRefreshedUi()
      Log.d(TAG, "End consumption flow.")
    }

    override fun onPurchasesUpdated(purchases: List<Purchase>) {
      isGoldMonthlySubscribed = false
      isGoldYearlySubscribed = false

      for (purchase in purchases) {
        when (purchase.sku) {
          PremiumDelegate.SKU_ID -> {
            Log.d(TAG, "You are Premium! Congratulations!!!")
            isPremiumPurchased = true
          }
          GasDelegate.SKU_ID -> {
            Log.d(TAG, "We have gas. Consuming it.")
            // We should consume the purchase and fill up the tank once it was consumed
            mActivity.billingManager.consumeAsync(purchase.purchaseToken)
          }
          GoldMonthlyDelegate.SKU_ID -> isGoldMonthlySubscribed = true
          GoldYearlyDelegate.SKU_ID -> isGoldYearlySubscribed = true
        }
      }
      if (isPremiumPurchased || isGoldMonthlySubscribed || isGoldYearlySubscribed) {
        mTank = TANK_MAX
      }

      mActivity.showRefreshedUi()
    }
  }

  /**
   * Save current tank level to disc
   *
   * Note: In a real application, we recommend you save data in a secure way to
   * prevent tampering.
   * For simplicity in this sample, we simply store the data using a
   * SharedPreferences.
   */
  private fun saveData() {
    val spe = mActivity.getPreferences(MODE_PRIVATE).edit()
    spe.putInt("tank", mTank)
    spe.apply()
    Log.d(TAG, "Saved data: tank = " + mTank.toString())
  }

  private fun loadData() {
    val sp = mActivity.getPreferences(MODE_PRIVATE)
    mTank = sp.getInt("tank", 2)
    Log.d(TAG, "Loaded data: tank = " + mTank.toString())
  }

  companion object {
    private val TAG = "MainViewController"

    // Graphics for the gas gauge
    private val TANK_RES_IDS = intArrayOf(R.drawable.gas0, R.drawable.gas1, R.drawable.gas2, R.drawable.gas3, R.drawable.gas4)

    // How many units (1/4 tank is our unit) fill in the tank.
    private const val TANK_MAX = 4
  }
}