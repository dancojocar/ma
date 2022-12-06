/*
 * Copyright (C) 2017 Google Inc.
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
package com.example.android.appwidgetsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.appwidgetsample.databinding.ActivityMainBinding

/**
 * AppWidgetSample demonstrates app widgets, including:
 * - Creating a template app widget to an app.
 * - Updating the app widget periodically (every 30 minutes).
 * - Adding a button to the app widget that updates on demand.
 *
 *
 * MainActivity is unused and includes a message to add the app widget to
 * the user's home screen.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}