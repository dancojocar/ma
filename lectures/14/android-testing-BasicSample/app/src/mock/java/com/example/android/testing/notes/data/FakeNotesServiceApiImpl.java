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

package com.example.android.testing.notes.data;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Fake implementation of {@link NotesServiceApi} to inject a fake service in a hermetic test.
 */
public class FakeNotesServiceApiImpl implements NotesServiceApi {

  // TODO replace this with a new test specific data set.
  private static final ArrayMap<String, Note> NOTES_SERVICE_DATA = new ArrayMap<>();

  @Override
  public void getAllNotes(NotesServiceCallback<List<Note>> callback) {
    callback.onLoaded(Lists.newArrayList(NOTES_SERVICE_DATA.values()));
  }

  @Override
  public void getNote(String noteId, NotesServiceCallback<Note> callback) {
    Note note = NOTES_SERVICE_DATA.get(noteId);
    callback.onLoaded(note);
  }

  @Override
  public void saveNote(Note note) {
    NOTES_SERVICE_DATA.put(note.getId(), note);
  }

  @VisibleForTesting
  public static void addNotes(Note... notes) {
    for (Note note : notes) {
      NOTES_SERVICE_DATA.put(note.getId(), note);
    }
  }
}
