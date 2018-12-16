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

package ro.ubbcluj.cs.ds.skulist

import com.android.billingclient.api.BillingClient.BillingResponse

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.SkuDetailsResponseListener

import ro.ubbcluj.cs.ds.R
import ro.ubbcluj.cs.ds.billing.BillingProvider
import ro.ubbcluj.cs.ds.skulist.row.SkuRowData
import ro.ubbcluj.cs.ds.skulist.row.UiManager

import java.util.ArrayList

/**
 * Displays a screen with various in-app purchase and subscription options
 */
class AcquireFragment : DialogFragment() {

  private var mRecyclerView: RecyclerView? = null
  private var mAdapter: SkusAdapter? = null
  private var mLoadingView: View? = null
  private var mErrorTextView: TextView? = null
  private var mBillingProvider: BillingProvider? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.acquire_fragment, container, false)
    mErrorTextView = root.findViewById(R.id.error_textview)
    mRecyclerView = root.findViewById(R.id.list)
    mLoadingView = root.findViewById(R.id.screen_wait)
    if (mBillingProvider != null) {
      handleManagerAndUiReady()
    }
    // Setup a toolbar for this fragment
    val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
    toolbar.setNavigationIcon(R.drawable.ic_arrow_up)
    toolbar.setNavigationOnClickListener { dismiss() }
    toolbar.setTitle(R.string.button_purchase)
    return root
  }

  /**
   * Refreshes this fragment's UI
   */
  fun refreshUI() {
    Log.d(TAG, "Looks like purchases list might have been updated - refreshing the UI")
    if (mAdapter != null) {
      mAdapter!!.notifyDataSetChanged()
    }
  }

  /**
   * Notifies the fragment that billing manager is ready and provides a BillingProviders
   * instance to access it
   */
  fun onManagerReady(billingProvider: BillingProvider) {
    mBillingProvider = billingProvider
    if (mRecyclerView != null) {
      handleManagerAndUiReady()
    }
  }

  /**
   * Enables or disables "please wait" screen.
   */
  private fun setWaitScreen(set: Boolean) {
    mRecyclerView!!.visibility = if (set) View.GONE else View.VISIBLE
    mLoadingView!!.visibility = if (set) View.VISIBLE else View.GONE
  }

  /**
   * Executes query for SKU details at the background thread
   */
  private fun handleManagerAndUiReady() {
    // If Billing Manager was successfully initialized - start querying for SKUs
    setWaitScreen(true)
    querySkuDetails()
  }

  private fun displayAnErrorIfNeeded() {
    if (activity == null || activity!!.isFinishing) {
      Log.i(TAG, "No need to show an error - activity is finishing already")
      return
    }

    mLoadingView!!.visibility = View.GONE
    mErrorTextView!!.visibility = View.VISIBLE
    val billingResponseCode = mBillingProvider!!.billingManager
        .billingClientResponseCode

    when (billingResponseCode) {
      BillingResponse.OK ->
        // If manager was connected successfully, then show no SKUs error
        mErrorTextView!!.text = getText(R.string.error_no_skus)
      BillingResponse.BILLING_UNAVAILABLE -> mErrorTextView!!.text = getText(R.string.error_billing_unavailable)
      else -> mErrorTextView!!.text = getText(R.string.error_billing_default)
    }

  }

  /**
   * Queries for in-app and subscriptions SKU details and updates an adapter with new data
   */
  private fun querySkuDetails() {
    val startTime = System.currentTimeMillis()

    Log.d(TAG, "querySkuDetails() got subscriptions and inApp SKU details lists for: "
        + (System.currentTimeMillis() - startTime) + "ms")

    if (activity != null && !activity!!.isFinishing) {
      val dataList = ArrayList<SkuRowData>()
      mAdapter = SkusAdapter()
      val uiManager = createUiManager(mAdapter!!, mBillingProvider!!)
      mAdapter!!.setUiManager(uiManager)
      // Filling the list with all the data to render subscription rows
      val subscriptionsSkus = uiManager.delegatesFactory
          .getSkuList(SkuType.SUBS)
      Log.d(TAG, "subscriptions: $subscriptionsSkus")
      addSkuRows(dataList, subscriptionsSkus, SkuType.SUBS, Runnable {
        // Once we added all the subscription items, fill the in-app items rows below
        val inAppSkus = uiManager.delegatesFactory
            .getSkuList(SkuType.INAPP)
        addSkuRows(dataList, inAppSkus, SkuType.INAPP, null)
      })
    }
  }

  private fun addSkuRows(inList: MutableList<SkuRowData>, skusList: List<String>,
                         @SkuType billingType: String, executeWhenFinished: Runnable?) {
    Log.i(TAG, "billingType0: $billingType list: $skusList")

    mBillingProvider!!.billingManager.querySkuDetailsAsync(billingType, skusList,
        SkuDetailsResponseListener { responseCode, skuDetailsList ->
          Log.d(TAG, "received: $responseCode list: $skuDetailsList")

          if (responseCode != BillingResponse.OK) {
            Log.w(TAG, "Unsuccessful query for type: " + billingType
                + ". Error code: " + responseCode)
          } else if (skuDetailsList != null && skuDetailsList.size > 0) {
            Log.i(TAG, "billingType: $billingType")
            // If we successfully got SKUs, add a header in front of the row
            @StringRes val stringRes = if (SkuType.INAPP == billingType)
              R.string.header_inapp
            else
              R.string.header_subscriptions
            inList.add(SkuRowData(title = getString(stringRes)))
            // Then fill all the other rows
            for (details in skuDetailsList) {
              Log.i(TAG, "Adding sku: $details")
              inList.add(SkuRowData(details, SkusAdapter.TYPE_NORMAL,
                  billingType))
            }

            if (inList.size == 0) {
              displayAnErrorIfNeeded()
            } else {
              if (mRecyclerView!!.adapter == null) {
                mRecyclerView!!.adapter = mAdapter
                val res = context!!.resources
                mRecyclerView!!.addItemDecoration(CardsWithHeadersDecoration(
                    mAdapter!!, res.getDimension(R.dimen.header_gap).toInt(),
                    res.getDimension(R.dimen.row_gap).toInt()))
                mRecyclerView!!.layoutManager = LinearLayoutManager(context)
              }

              mAdapter!!.updateData(inList)
              setWaitScreen(false)
            }

          }

          executeWhenFinished?.run()
        })
  }

  protected fun createUiManager(adapter: SkusAdapter, provider: BillingProvider): UiManager {
    return UiManager(adapter, provider)
  }

  companion object {
    private val TAG = "AcquireFragment"
  }
}