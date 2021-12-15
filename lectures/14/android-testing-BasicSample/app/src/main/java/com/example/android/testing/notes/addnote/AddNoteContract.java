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

package com.example.android.testing.notes.addnote;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface AddNoteContract {

  interface View {

    void showEmptyNoteError();

    void showNotesList();

    void openCamera(String saveTo);

    void showImagePreview(@NonNull String uri);

    void showImageError();
  }

  interface UserActionsListener {

    void saveNote(String title, String description);

    void takePicture() throws IOException;

    void imageAvailable();

    void imageCaptureFailed();
  }
}
