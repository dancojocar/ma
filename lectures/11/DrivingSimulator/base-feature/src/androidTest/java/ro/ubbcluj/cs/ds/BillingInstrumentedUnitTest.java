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
package ro.ubbcluj.cs.ds;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import com.android.billingclient.api.BillingClient.SkuType;
import ro.ubbcluj.cs.ds.MainViewController;
import ro.ubbcluj.cs.ds.R;
import ro.ubbcluj.cs.ds.billing.BillingConstants;
import ro.ubbcluj.cs.ds.billing.BillingManager;
import ro.ubbcluj.cs.ds.skulist.SkusAdapter;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation tests for billing UI.
 *
 * Testing Support Library:
 *  https://d.android.com/topic/libraries/testing-support-library/
 *
 * Testing Fundamentals:
 *     https://d.android.com/tools/testing/testing_android.html
 *
 * Instrumented Unit tests:
 *     https://d.android.com/training/testing/unit-testing/instrumented-unit-tests.html
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class BillingInstrumentedUnitTest {

    private static final String TAG = "BillingInstrumented";

    @Rule
    public ActivityTestRule<TrivialDriveActivity> mActivityRule =
            new ActivityTestRule<>(TrivialDriveActivity.class);

    @Test
    public void testPremiumStatus() {
        TrivialDriveActivity activity = mActivityRule.getActivity();
        ImageView premiumView = (ImageView) activity.findViewById(R.id.free_or_premium);
        int resId = (int) premiumView.getTag();

        Log.d(TAG, "Starting premium test! Checking premium/free image...");

        if (activity.isPremiumPurchased()) {
            assertTrue("Didn't show a Premium image for VIP user", resId == R.drawable.premium);
        } else {
            assertTrue("Didn't show a Free image for a normal user", resId == R.drawable.free);
        }
    }

    @Test
    public void testGasUsage() {
        final TrivialDriveActivity activity = mActivityRule.getActivity();
        MainViewController controller = activity.getViewController();
        ImageView gasView = (ImageView) activity.findViewById(R.id.gas_gauge);

        Log.d(TAG, "Starting the test! Checking shown gas level...");

        @DrawableRes int initialTankResId = (int) gasView.getTag();
        assertTrue("Didn't show correct gas level to the user",
                initialTankResId == controller.getTankResId());

        Log.i(TAG, "Emulating click on gas consumption button");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onDriveButtonClicked();
            }
        });

        getInstrumentation().waitForIdleSync();

        if (initialTankResId == R.drawable.gas0) {
            checkDialogWithTextShown(activity, R.string.alert_no_gas);
            Log.i(TAG, "Checking that we didn't change gas level image for an empty tank");
            assertTrue("Did change gas level when it was already empty",
                    initialTankResId == (int) gasView.getTag());
        } else {
            checkDialogWithTextShown(activity, R.string.alert_drove);
            Log.i(TAG, "Checking that we did change gas level image");
            assertTrue("Didn't decrease gas level for no subscription",
                    initialTankResId != (int) gasView.getTag());
            // Continue testing gas usage until the tank is empty
            testGasUsage();
        }
    }

    @Test
    public void testSkuList() {
        final TrivialDriveActivity activity = mActivityRule.getActivity();

        Log.d(TAG, "Starting Sku list test! Emulating click on purchase button");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onPurchaseButtonClicked();
            }
        });

        getInstrumentation().waitForIdleSync();

        DialogFragment acquireFragment = activity.getDialogFragment();
        assertTrue(acquireFragment != null);
        assertTrue(acquireFragment.isVisible());

        List<String> inAppList = BillingConstants.INSTANCE.getSkuList(SkuType.INAPP);

        // Testing subscriptions if they are supported for current client
        BillingManager billingManager = activity.getBillingManager();
        boolean areSubscriptionsSupported = billingManager.areSubscriptionsSupported();

        getInstrumentation().waitForIdleSync();

        RecyclerView recyclerView = (RecyclerView) acquireFragment.getView().findViewById(
                R.id.list);
        SkusAdapter adapter = (SkusAdapter) recyclerView.getAdapter();
        checkAdapterContainsSKUs(adapter, inAppList);

        if (areSubscriptionsSupported) {
            List<String> subscriptionsList = BillingConstants.INSTANCE.getSkuList(SkuType.SUBS);
            checkAdapterContainsSKUs(adapter, subscriptionsList);
        }
    }

    /**
    * Check that we show an alert dialog with correct text
    */
    private void checkDialogWithTextShown(Activity activity, @StringRes int stringId) {
        String dialogText = activity.getString(stringId);
        Log.i(TAG, "Checking if we show a dialog with text: " + dialogText);
        onView(withText(dialogText)).check(matches(isDisplayed()));
    }

    /**
     * Check if adapter contains all the SKUs from the list
     */
    private void checkAdapterContainsSKUs(SkusAdapter adapter, List<String> skusList) {
        for (String skuId : skusList) {
            Log.i(TAG, "Checking that the adapter contains: " + skuId);
            boolean doesAdapterContainSku = false;
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (adapter.getData(i).getRowType() == SkusAdapter.Companion.getTYPE_NORMAL()
                        && adapter.getData(i).getSku().equals(skuId)) {
                    doesAdapterContainSku = true;
                }
            }
            // Check that adapter contains all the SKUs
            assertTrue(doesAdapterContainSku);
        }
    }
}