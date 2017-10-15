/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package ro.ubbcluj.cs.ma.ds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import ro.ubbcluj.cs.ma.ds.util.IabBroadcastReceiver;
import ro.ubbcluj.cs.ma.ds.util.IabBroadcastReceiver.IabBroadcastListener;
import ro.ubbcluj.cs.ma.ds.util.IabHelper;
import ro.ubbcluj.cs.ma.ds.util.IabHelper.IabAsyncInProgressException;
import ro.ubbcluj.cs.ma.ds.util.IabResult;
import ro.ubbcluj.cs.ma.ds.util.Inventory;
import ro.ubbcluj.cs.ma.ds.util.Purchase;
import timber.log.Timber;

/**
 * Example game using in-app billing version 3.
 * <p>
 * Before attempting to run this sample, please read the README file. It
 * contains important information on how to set up this project.
 * <p>
 * All the game-specific logic is implemented here in MainActivity, while the
 * general-purpose boilerplate that can be reused in any app is provided in the
 * classes in the util/ subdirectory. When implementing your own application,
 * you can copy over util/*.java to make use of those utility classes.
 * <p>
 * This game is a simple "driving" game where the player can buy gas
 * and drive. The car has a tank which stores gas. When the player purchases
 * gas, the tank fills up (1/4 tank at a time). When the player drives, the gas
 * in the tank diminishes (also 1/4 tank at a time).
 * <p>
 * The user can also purchase a "premium upgrade" that gives them a red car
 * instead of the standard blue one (exciting!).
 * <p>
 * The user can also purchase a subscription ("infinite gas") that allows them
 * to drive without using up any gas while that subscription is active.
 * <p>
 * It's important to note the consumption mechanics for each item.
 * <p>
 * PREMIUM: the item is purchased and NEVER consumed. So, after the original
 * purchase, the player will always own that item. The application knows to
 * display the red car instead of the blue one because it queries whether
 * the premium "item" is owned or not.
 * <p>
 * INFINITE GAS: this is a subscription, and subscriptions can't be consumed.
 * <p>
 * GAS: when gas is purchased, the "gas" item is then owned. We consume it
 * when we apply that item's effects to our app's world, which to us means
 * filling up 1/4 of the tank. This happens immediately after purchase!
 * It's at this point (and not when the user drives) that the "gas"
 * item is CONSUMED. Consumption should always happen when your game
 * world was safely updated to apply the effect of the purchase. So,
 * in an example scenario:
 * <p>
 * BEFORE:      tank at 1/2
 * ON PURCHASE: tank at 1/2, "gas" item is owned
 * IMMEDIATELY: "gas" is consumed, tank goes to 3/4
 * AFTER:       tank at 3/4, "gas" item NOT owned any more
 * <p>
 * Another important point to notice is that it may so happen that
 * the application crashed (or anything else happened) after the user
 * purchased the "gas" item, but before it was consumed. That's why,
 * on startup, we check if we own the "gas" item, and, if so,
 * we have to apply its effects to our world and consume it. This
 * is also very important!
 */
public class MainActivity extends Activity implements IabBroadcastListener,
    OnClickListener {
  // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
  static final String SKU_PREMIUM = "premium";
  static final String SKU_GAS = "gas";
  // SKU for our subscription (infinite gas)
  static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
  static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";
  // (arbitrary) request code for the purchase flow
  static final int RC_REQUEST = 10001;
  // How many units (1/4 tank is our unit) fill in the tank.
  static final int TANK_MAX = 4;
  // Graphics for the gas gauge
  static int[] TANK_RES_IDS = {R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
      R.drawable.gas3, R.drawable.gas4};
  // Does the user have the premium upgrade?
  boolean mIsPremium = false;
  // Does the user have an active subscription to the infinite gas plan?
  boolean mSubscribedToInfiniteGas = false;
  // Will the subscription auto-renew?
  boolean mAutoRenewEnabled = false;
  // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
  String mInfiniteGasSku = "";
  String mFirstChoiceSku = "";
  String mSecondChoiceSku = "";
  // Used to select between purchasing gas on a monthly or yearly basis
  String mSelectedSubscriptionPeriod = "";
  // Current amount of gas in tank, in units
  int mTank;

  // The helper object
  IabHelper mHelper;

  // Provides purchase notification while this app is running
  IabBroadcastReceiver mBroadcastReceiver;
  // Called when consumption is complete
  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
      Timber.d("Consumption finished. Purchase: " + purchase + ", result: " + result);

      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      // We know this is the "gas" sku because it's the only one we consume,
      // so we don't check which sku was consumed. If you have more than one
      // sku, you probably should check...
      if (result.isSuccess()) {
        // successfully consumed, so we apply the effects of the item in our
        // game world's logic, which in our case means filling the gas tank a bit
        Timber.d("Consumption successful. Provisioning.");
        mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
        saveData();
        alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
      } else {
        complain("Error while consuming: " + result);
      }
      updateUi();
      setWaitScreen(false);
      Timber.d("End consumption flow.");
    }
  };
  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
      Timber.d("Query inventory finished.");

      // Have we been disposed of in the meantime? If so, quit.
      if (mHelper == null) return;

      // Is it a failure?
      if (result.isFailure()) {
        complain("Failed to query inventory: " + result);
        return;
      }

      Timber.d("Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

      // Do we have the premium upgrade?
      Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
      mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
      Timber.d("User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

      // First find out which subscription is auto renewing
      Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
      Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
      if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
        mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
        mAutoRenewEnabled = true;
      } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
        mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
        mAutoRenewEnabled = true;
      } else {
        mInfiniteGasSku = "";
        mAutoRenewEnabled = false;
      }

      // The user is subscribed if either subscription exists, even if neither is auto
      // renewing
      mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
          || (gasYearly != null && verifyDeveloperPayload(gasYearly));
      Timber.d("User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
          + " infinite gas subscription.");
      if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

      // Check for gas delivery -- if we own gas, we should fill up the tank immediately
      Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
      if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
        Timber.d("We have gas. Consuming it.");
        try {
          mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
        } catch (IabAsyncInProgressException e) {
          complain("Error consuming gas. Another async operation in progress.");
        }
        return;
      }

      updateUi();
      setWaitScreen(false);
      Timber.d("Initial inventory query finished; enabling main UI.");
    }
  };
  // Callback for when a purchase is finished
  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
      Timber.d("Purchase finished: " + result + ", purchase: " + purchase);

      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      if (result.isFailure()) {
        complain("Error purchasing: " + result);
        setWaitScreen(false);
        return;
      }
      if (!verifyDeveloperPayload(purchase)) {
        complain("Error purchasing. Authenticity verification failed.");
        setWaitScreen(false);
        return;
      }

      Timber.d("Purchase successful.");

      switch (purchase.getSku()) {
        case SKU_GAS:
          // bought 1/4 tank of gas. So consume it.
          Timber.d("Purchase is gas. Starting gas consumption.");
          try {
            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
          } catch (IabAsyncInProgressException e) {
            complain("Error consuming gas. Another async operation in progress.");
            setWaitScreen(false);
            return;
          }
          break;
        case SKU_PREMIUM:
          // bought the premium upgrade!
          Timber.d("Purchase is premium upgrade. Congratulating user.");
          alert("Thank you for upgrading to premium!");
          mIsPremium = true;
          updateUi();
          setWaitScreen(false);
          break;
        case SKU_INFINITE_GAS_MONTHLY:
        case SKU_INFINITE_GAS_YEARLY:
          // bought the infinite gas subscription
          Timber.d("Infinite gas subscription purchased.");
          alert("Thank you for subscribing to infinite gas!");
          mSubscribedToInfiniteGas = true;
          mAutoRenewEnabled = purchase.isAutoRenewing();
          mInfiniteGasSku = purchase.getSku();
          mTank = TANK_MAX;
          updateUi();
          setWaitScreen(false);
          break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // load game data
    loadData();

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjEq0CDSDhflhA49oUVT" +
        "d5ztVhhZEEq3HOYyKJYebljST64FWWer/bguqvpniTJ39rKD/REz06iAxnY++BcSfk1nBCsTbuDxkbmmmwXEBo6gXjy" +
        "kY9v9ToEUaLi9fQCGhOYa/oVdkrSUMhkSbeA94WUCZvkf9hjH97TRc73IM+CCYx2sFHwEupeJMzvWjJstFp5GNzjpWj" +
        "LY8Xg61bBhC2z5emGGleQuaD3PGh+5Q+7LI2r1rvtvVjcMdDLuAcA4iiijf/f+pFE7BVb/JvaR63T8K3GWZYMfrGfWm" +
        "h0KU/Uvyz6VCv3GgEJtGqr2wXaP9mm0p5F+IyQp7bwzptQQkUQIDAQAB";

    // Create the helper, passing it our context and the public key to verify signatures with
    Timber.d("Creating IAB helper.");
    mHelper = new IabHelper(this, base64EncodedPublicKey);

    // Start setup. This is asynchronous and the specified listener
    // will be called once setup completes.
    Timber.d("Starting setup.");
    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        Timber.d("Setup finished.");

        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          complain("Problem setting up in-app billing: " + result);
          return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (mHelper == null) return;

        // Important: Dynamically register for broadcast messages about updated purchases.
        // We register the receiver here instead of as a <receiver> in the Manifest
        // because we always call getPurchases() at startup, so therefore we can ignore
        // any broadcasts sent while the app isn't running.
        // Note: registering this listener in an Activity is a bad idea, but is done here
        // because this is a SAMPLE. Regardless, the receiver must be registered after
        // IabHelper is setup, but before first call to getPurchases().
        mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(mBroadcastReceiver, broadcastFilter);

        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        Timber.d("Setup successful. Querying inventory.");
        try {
          mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabAsyncInProgressException e) {
          complain("Error querying inventory. Another async operation in progress.");
        }
      }
    });

    MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

    AdView mAdView = (AdView) findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
  }

  @Override
  public void receivedBroadcast() {
    // Received a broadcast notification that the inventory of items has changed
    Timber.d("Received broadcast notification. Querying inventory.");
    try {
      mHelper.queryInventoryAsync(mGotInventoryListener);
    } catch (IabAsyncInProgressException e) {
      complain("Error querying inventory. Another async operation in progress.");
    }
  }

  // User clicked the "Buy Gas" button
  public void onBuyGasButtonClicked(View arg0) {
    Timber.d("Buy gas button clicked.");

    if (mSubscribedToInfiniteGas) {
      complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
      return;
    }

    if (mTank >= TANK_MAX) {
      complain("Your tank is full. Drive around a bit!");
      return;
    }

    // launch the gas purchase UI flow.
    // We will be notified of completion via mPurchaseFinishedListener
    setWaitScreen(true);
    Timber.d("Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    try {
      mHelper.launchPurchaseFlow(this, SKU_GAS, RC_REQUEST, mPurchaseFinishedListener, payload);
    } catch (IabAsyncInProgressException e) {
      complain("Error launching purchase flow. Another async operation in progress.");
      setWaitScreen(false);
    }
  }

  // User clicked the "Upgrade to Premium" button.
  public void onUpgradeAppButtonClicked(View arg0) {
    Timber.d("Upgrade button clicked; launching purchase flow for upgrade.");
    setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    try {
      mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
          mPurchaseFinishedListener, payload);
    } catch (IabAsyncInProgressException e) {
      complain("Error launching purchase flow. Another async operation in progress.");
      setWaitScreen(false);
    }
  }

  // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
  // flow for subscription.
  public void onInfiniteGasButtonClicked(View arg0) {
    if (!mHelper.subscriptionsSupported()) {
      complain("Subscriptions not supported on your device yet. Sorry!");
      return;
    }

    CharSequence[] options;
    if (!mSubscribedToInfiniteGas || !mAutoRenewEnabled) {
      // Both subscription options should be available
      options = new CharSequence[2];
      options[0] = getString(R.string.subscription_period_monthly);
      options[1] = getString(R.string.subscription_period_yearly);
      mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
      mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;
    } else {
      // This is the subscription upgrade/downgrade path, so only one option is valid
      options = new CharSequence[1];
      if (mInfiniteGasSku.equals(SKU_INFINITE_GAS_MONTHLY)) {
        // Give the option to upgrade to yearly
        options[0] = getString(R.string.subscription_period_yearly);
        mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
      } else {
        // Give the option to downgrade to monthly
        options[0] = getString(R.string.subscription_period_monthly);
        mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
      }
      mSecondChoiceSku = "";
    }

    int titleResId;
    if (!mSubscribedToInfiniteGas) {
      titleResId = R.string.subscription_period_prompt;
    } else if (!mAutoRenewEnabled) {
      titleResId = R.string.subscription_resignup_prompt;
    } else {
      titleResId = R.string.subscription_update_prompt;
    }

    Builder builder = new Builder(this);
    builder.setTitle(titleResId)
        .setSingleChoiceItems(options, 0 /* checkedItem */, this)
        .setPositiveButton(R.string.subscription_prompt_continue, this)
        .setNegativeButton(R.string.subscription_prompt_cancel, this);
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  @Override
  public void onClick(DialogInterface dialog, int id) {
    if (id == 0 /* First choice item */) {
      mSelectedSubscriptionPeriod = mFirstChoiceSku;
    } else if (id == 1 /* Second choice item */) {
      mSelectedSubscriptionPeriod = mSecondChoiceSku;
    } else if (id == DialogInterface.BUTTON_POSITIVE /* continue button */) {
            /* TODO: for security, generate your payload here for verification. See the comments on
             *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
             *        an empty string, but on a production app you should carefully generate
             *        this. */
      String payload = "";

      if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
        // The user has not changed from the default selection
        mSelectedSubscriptionPeriod = mFirstChoiceSku;
      }

      List<String> oldSkus = null;
      if (!TextUtils.isEmpty(mInfiniteGasSku)
          && !mInfiniteGasSku.equals(mSelectedSubscriptionPeriod)) {
        // The user currently has a valid subscription, any purchase action is going to
        // replace that subscription
        oldSkus = new ArrayList<>();
        oldSkus.add(mInfiniteGasSku);
      }

      setWaitScreen(true);
      Timber.d("Launching purchase flow for gas subscription.");
      try {
        mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
            oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
      } catch (IabAsyncInProgressException e) {
        complain("Error launching purchase flow. Another async operation in progress.");
        setWaitScreen(false);
      }
      // Reset the dialog options
      mSelectedSubscriptionPeriod = "";
      mFirstChoiceSku = "";
      mSecondChoiceSku = "";
    } else if (id != DialogInterface.BUTTON_NEGATIVE) {
      // There are only four buttons, this should not happen
      Timber.d("Unknown button clicked in subscription dialog: " + id);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Timber.d("onActivityResult(" + requestCode + "," + resultCode + "," + data);
    if (mHelper == null) return;

    // Pass on the activity result to the helper for handling
    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
      // not handled, so handle it ourselves (here's where you'd
      // perform any handling of activity results not related to in-app
      // billing...
      super.onActivityResult(requestCode, resultCode, data);
    } else {
      Timber.d("onActivityResult handled by IABUtil.");
    }
  }

  /**
   * Verifies the developer payload of a purchase.
   */
  boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

    return true;
  }

  // Drive button clicked. Burn gas!
  public void onDriveButtonClicked(View arg0) {
    Timber.d("Drive button clicked.");
    if (!mSubscribedToInfiniteGas && mTank <= 0)
      alert("Oh, no! You are out of gas! Try buying some!");
    else {
      if (!mSubscribedToInfiniteGas) --mTank;
      saveData();
      alert("Vroooom, you drove a few miles.");
      updateUi();
      Timber.d("Vrooom. Tank is now " + mTank);
    }
  }

  // We're being destroyed. It's important to dispose of the helper here!
  @Override
  public void onDestroy() {
    super.onDestroy();

    // very important:
    if (mBroadcastReceiver != null) {
      unregisterReceiver(mBroadcastReceiver);
    }

    // very important:
    Timber.d("Destroying helper.");
    if (mHelper != null) {
      mHelper.disposeWhenFinished();
      mHelper = null;
    }
  }

  // updates UI to reflect model
  public void updateUi() {
    // update the car color to reflect premium status or lack thereof
    ((ImageView) findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);

    // "Upgrade" button is only visible if the user is not premium
    findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);

    ImageView infiniteGasButton = (ImageView) findViewById(R.id.infinite_gas_button);
    if (mSubscribedToInfiniteGas) {
      // If subscription is active, show "Manage Infinite Gas"
      infiniteGasButton.setImageResource(R.drawable.manage_infinite_gas);
    } else {
      // The user does not have infinite gas, show "Get Infinite Gas"
      infiniteGasButton.setImageResource(R.drawable.get_infinite_gas);
    }

    // update gas gauge to reflect tank status
    if (mSubscribedToInfiniteGas) {
      ((ImageView) findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
    } else {
      int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
      ((ImageView) findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
    }
  }

  // Enables or disables the "please wait" screen.
  void setWaitScreen(boolean set) {
    findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
    findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
  }

  void complain(String message) {
    Timber.e("**** TrivialDrive Error: " + message);
    alert("Error: " + message);
  }

  void alert(String message) {
    AlertDialog.Builder bld = new AlertDialog.Builder(this);
    bld.setMessage(message);
    bld.setNeutralButton("OK", null);
    Timber.d("Showing alert dialog: " + message);
    bld.create().show();
  }

  void saveData() {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

    SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
    spe.putInt("tank", mTank);
    spe.apply();
    Timber.d("Saved data: tank = " + String.valueOf(mTank));
  }

  void loadData() {
    SharedPreferences sp = getPreferences(MODE_PRIVATE);
    mTank = sp.getInt("tank", 2);
    Timber.d("Loaded data: tank = " + String.valueOf(mTank));
  }
}
