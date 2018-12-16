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

import com.android.billingclient.api.BillingClient.BillingResponse
import ro.ubbcluj.cs.ds.billing.BillingManager.Companion.BILLING_MANAGER_NOT_INITIALIZED

import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.ImageView

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

import ro.ubbcluj.cs.ds.billing.BillingManager
import ro.ubbcluj.cs.ds.billing.BillingProvider
import ro.ubbcluj.cs.ds.skulist.AcquireFragment

/**
 * Example game using in-app billing version 3.
 *
 *
 * Before attempting to run this sample, please read the README file. It
 * contains important information on how to set up this project.
 *
 *
 * All the game-specific logic is implemented here in BaseGamePlayActivity, while billing-specific
 * logic is moved into billing package. Don't forget to add a billing-library module which
 * it depends on.
 *
 *
 * This game is a simple "driving" game where the player can buy gas
 * and drive. The car has a tank which stores gas. When the player purchases
 * gas, the tank fills up (1/4 tank at a time). When the player drives, the gas
 * in the tank diminishes (also 1/4 tank at a time).
 *
 *
 * The user can also purchase a "premium upgrade" that gives them a red car
 * instead of the standard blue one (exciting!).
 *
 *
 * The user can also purchase a subscription ("infinite gas") that allows them
 * to drive without using up any gas while that subscription is active.
 *
 *
 * It's important to note the consumption mechanics for each item.
 *
 *
 * PREMIUM: the item is purchased and NEVER consumed. So, after the original
 * purchase, the player will always own that item. The application knows to
 * display the red car instead of the blue one because it queries whether
 * the premium "item" is owned or not.
 *
 *
 * INFINITE GAS: this is a subscription, and subscriptions can't be consumed.
 *
 *
 * GAS: when gas is purchased, the "gas" item is then owned. We consume it
 * when we apply that item's effects to our app's world, which to us means
 * filling up 1/4 of the tank. This happens immediately after purchase!
 * It's at this point (and not when the user drives) that the "gas"
 * item is CONSUMED. Consumption should always happen when your game
 * world was safely updated to apply the effect of the purchase. So,
 * in an example scenario:
 *
 *
 * BEFORE:      tank at 1/2
 * ON PURCHASE: tank at 1/2, "gas" item is owned
 * IMMEDIATELY: "gas" is consumed, tank goes to 3/4
 * AFTER:       tank at 3/4, "gas" item NOT owned any more
 *
 *
 * Another important point to notice is that it may so happen that
 * the application crashed (or anything else happened) after the user
 * purchased the "gas" item, but before it was consumed. That's why,
 * on startup, we check if we own the "gas" item, and, if so,
 * we have to apply its effects to our world and consume it. This
 * is also very important!
 */
abstract class BaseGamePlayActivity : FragmentActivity(), BillingProvider {

  override lateinit var billingManager: BillingManager
  private var mAcquireFragment: AcquireFragment? = null
  private var viewController: MainViewController? = null

  private var mScreenWait: View? = null
  private var mScreenMain: View? = null
  private var mCarImageView: ImageView? = null
  private var mGasImageView: ImageView? = null

  override val isPremiumPurchased: Boolean
    get() = viewController!!.isPremiumPurchased

  override val isGoldMonthlySubscribed: Boolean
    get() = viewController!!.isGoldMonthlySubscribed

  override val isGoldYearlySubscribed: Boolean
    get() = viewController!!.isGoldYearlySubscribed

  override val isTankFull: Boolean
    get() = viewController!!.isTankFull

  protected abstract val layoutResId: Int

  private val isAcquireFragmentShown: Boolean
    get() = mAcquireFragment != null && mAcquireFragment!!.isVisible

  val dialogFragment: DialogFragment?
    get() = mAcquireFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(layoutResId)

    // Start the controller and load game data
    viewController = MainViewController(this)

    if (packageName.startsWith(DEFAULT_PACKAGE_PREFIX)) {
      throw RuntimeException("Please change the sample's package name!")
    }

    // Try to restore dialog fragment if we were showing it prior to screen rotation
    if (savedInstanceState != null) {
      mAcquireFragment = supportFragmentManager
          .findFragmentByTag(DIALOG_TAG) as AcquireFragment?
    }

    // Create and initialize BillingManager which talks to BillingLibrary
    billingManager = BillingManager(this, viewController!!.updateListener)

    mScreenWait = findViewById(R.id.screen_wait)
    mScreenMain = findViewById(R.id.screen_main)
    mCarImageView = findViewById(R.id.free_or_premium)
    mGasImageView = findViewById(R.id.gas_gauge)

    // Specify purchase and drive buttons listeners
    // Note: This couldn't be done inside *.xml for Android TV since TV layout is inflated
    // via AppCompat
    findViewById<View>(R.id.button_purchase).setOnClickListener { onPurchaseButtonClicked() }
    findViewById<View>(R.id.button_drive).setOnClickListener { onDriveButtonClicked() }


    val mAdView = findViewById<AdView>(R.id.adView)
    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)
  }

  override fun onResume() {
    super.onResume()
    // Note: We query purchases in onResume() to handle purchases completed while the activity
    // is inactive. For example, this can happen if the activity is destroyed during the
    // purchase flow. This ensures that when the activity is resumed it reflects the user's
    // current purchases.
    if (billingManager.billingClientResponseCode == BillingResponse.OK) {
      billingManager.queryPurchases()
    }
  }

  /**
   * User clicked the "Buy Gas" button - show a purchase dialog with all available SKUs
   */
  fun onPurchaseButtonClicked() {
    Log.d(TAG, "Purchase button clicked.")

    if (mAcquireFragment == null) {
      mAcquireFragment = AcquireFragment()
    }

    if (!isAcquireFragmentShown) {
      mAcquireFragment!!.show(supportFragmentManager, DIALOG_TAG)

      if (billingManager.billingClientResponseCode > BILLING_MANAGER_NOT_INITIALIZED) {
        mAcquireFragment!!.onManagerReady(this)
      }
    }
  }

  /**
   * Drive button clicked. Burn gas!
   */
  fun onDriveButtonClicked() {
    Log.d(TAG, "Drive button clicked.")

    if (viewController!!.isTankEmpty) {
      alert(R.string.alert_no_gas)
    } else {
      viewController!!.useGas()
      alert(R.string.alert_drove)
      updateUi()
    }
  }

  public override fun onDestroy() {
    Log.d(TAG, "Destroying helper.")
    billingManager.destroy()
    super.onDestroy()
  }

  /**
   * Remove loading spinner and refresh the UI
   */
  fun showRefreshedUi() {
    setWaitScreen(false)
    updateUi()
    if (mAcquireFragment != null) {
      mAcquireFragment!!.refreshUI()
    }
  }

  /**
   * Show an alert dialog to the user
   *
   * @param messageId     String id to display inside the alert dialog
   * @param optionalParam Optional attribute for the string
   */
  @UiThread
  @JvmOverloads
  internal fun alert(@StringRes messageId: Int, optionalParam: Any? = null) {
    if (Looper.getMainLooper().thread !== Thread.currentThread()) {
      throw RuntimeException("Dialog could be shown only from the main thread")
    }

    val bld = AlertDialog.Builder(this)
    bld.setNeutralButton("OK", null)

    if (optionalParam == null) {
      bld.setMessage(messageId)
    } else {
      bld.setMessage(resources.getString(messageId, optionalParam))
    }

    bld.create().show()
  }

  internal fun onBillingManagerSetupFinished() {
    if (mAcquireFragment != null) {
      mAcquireFragment!!.onManagerReady(this)
    }
  }

  /**
   * Enables or disables the "please wait" screen.
   */
  private fun setWaitScreen(set: Boolean) {
    mScreenMain!!.visibility = if (set) View.GONE else View.VISIBLE
    mScreenWait!!.visibility = if (set) View.VISIBLE else View.GONE
  }

  /**
   * Sets image resource and also adds a tag to be able to verify that image is correct in tests
   */
  private fun setImageResourceWithTestTag(imageView: ImageView, @DrawableRes resId: Int) {
    imageView.setImageResource(resId)
    imageView.tag = resId
  }

  /**
   * Update UI to reflect model
   */
  @UiThread
  private fun updateUi() {
    Log.d(TAG, "Updating the UI. Thread: " + Thread.currentThread().name)

    // Update car's color to reflect premium status or lack thereof
    setImageResourceWithTestTag(mCarImageView!!, if (isPremiumPurchased)
      R.drawable.premium
    else
      R.drawable.free)

    // Update gas gauge to reflect tank status
    setImageResourceWithTestTag(mGasImageView!!, viewController!!.tankResId)

    if (isGoldMonthlySubscribed || isGoldYearlySubscribed) {
      mCarImageView!!.setBackgroundColor(ContextCompat.getColor(this, R.color.gold))
    }
  }

  companion object {
    // Debug tag, for logging
    private val TAG = "BaseGamePlayActivity"

    // Tag for a dialog that allows us to find it when screen was rotated
    private const val DIALOG_TAG = "dialog"

    // Default sample's package name to check if you changed it
    private const val DEFAULT_PACKAGE_PREFIX = "com.example"
  }
}
