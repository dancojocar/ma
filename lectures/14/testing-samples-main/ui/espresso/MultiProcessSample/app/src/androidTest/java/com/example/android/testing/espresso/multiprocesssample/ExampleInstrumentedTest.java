/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.testing.espresso.multiprocesssample;

import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";

    private static final String DEFAULT_PROC_NAME =
            "com.example.android.testing.espresso.multiprocesssample";

    @Before
    public void launchActivity() {
        ActivityScenario.launch(DefaultProcessActivity.class);
    }

    @Test
    public void verifyAssertingOnViewInRemoteProcessIsSuccessful() {
        Log.d(TAG, "Checking main process name...");
        onView(withId(R.id.textNamedProcess)).check(matches(withText(is(DEFAULT_PROC_NAME))));

        Log.d(TAG, "Starting activity in a secondary process...");
        onView(withId(R.id.startActivityBtn)).perform(click());

        Log.d(TAG, "Checking private process name...");
        onView(withId(R.id.textPrivateProcessName))
                .check(matches(withText(is(DEFAULT_PROC_NAME + ":PID2"))));

        Log.d(TAG, "Clicking list item in private process activity...");
        onData(allOf(instanceOf(String.class), is("Doppio"))).perform(click());

        Log.d(TAG, "Check selected text appears...");
        onView(withId(R.id.selectedListItemText)).check(matches(withText("Selected: Doppio")));
    }
}
