/*
 * Copyright 2017 Google Inc. All rights reserved.
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

import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_GAS;
import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_GOLD_MONTHLY;
import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_GOLD_YEARLY;
import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_PREMIUM;
import static org.junit.Assert.assertTrue;
import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_GAS;
import static ro.ubbcluj.cs.ds.billing.BillingConstants.SKU_PREMIUM;

import ro.ubbcluj.cs.ds.api.BillingClient;
import ro.ubbcluj.cs.ds.billing.BillingConstants;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for billing model.
 *
 * Note:
 *     To work on unit tests, switch the Test Artifact in the Build Variants view.
 *
 * Testing Fundamentals:
 *     http://d.android.com/tools/testing/testing_android.html
 *
 * Local Unit tests:
 *     https://d.android.com/training/testing/unit-testing/local-unit-tests.html
 */
public class BillingUnitTest {
    @Test
    public void billingHelperData_isConsistent() throws Exception {
        List<String> inAppList = BillingConstants.getSkuList(BillingClient.SkuType.INAPP);
        assertTrue(inAppList.contains(SKU_PREMIUM));
        assertTrue(inAppList.contains(SKU_GAS));
        List<String> subscriptionsList = BillingConstants.getSkuList(BillingClient.SkuType.SUBS);
        assertTrue(subscriptionsList.contains(SKU_GOLD_MONTHLY));
        assertTrue(subscriptionsList.contains(SKU_GOLD_YEARLY));
    }
}
