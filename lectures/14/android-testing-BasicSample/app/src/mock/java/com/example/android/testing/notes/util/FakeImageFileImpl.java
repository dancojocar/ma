/*
 * Copyright 2015, The Android Open Source Project
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

package com.example.android.testing.notes.util;

import java.io.IOException;

/**
 * Fake implementation of {@link ImageFile} to inject a fake image in a hermetic test.
 */
public class FakeImageFileImpl extends ImageFileImpl {

  @Override
  public void create(String name, String extension) throws IOException {
    // Do nothing
  }

  @Override
  public String getPath() {
    return "file:///android_asset/atsl-logo.png";
  }

  @Override
  public boolean exists() {
    return true;
  }
}
