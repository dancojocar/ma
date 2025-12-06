/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.example.android.splashscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * Shows the app content that is commonly used in all of [DefaultActivity], [AnimatedActivity], and
 * [CustomActivity]. This also handles the custom dark mode on API level 31 and above.
 */
open class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    protected lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up 'core-splashscreen' to handle the splash screen in a backward compatible manner.
        splashScreen = installSplashScreen()

        // The splash screen remains on the screen as long as this condition is true.
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                MainScreen(viewModel)
            }
        }
    }
}
