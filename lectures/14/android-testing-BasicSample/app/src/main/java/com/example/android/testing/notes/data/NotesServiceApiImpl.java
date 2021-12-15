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

import android.os.Handler;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Notes Service API that adds a latency simulating network.
 */
public class NotesServiceApiImpl implements NotesServiceApi {

  private static final int SERVICE_LATENCY_IN_MILLIS = 2000;
  private static final ArrayMap<String, Note> NOTES_SERVICE_DATA =
      NotesServiceApiEndpoint.loadPersistedNotes();

  @Override
  public void getAllNotes(final NotesServiceCallback<List<Note>> callback) {
    // Simulate network by delaying the execution.
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        List<Note> notes = new ArrayList<>(NOTES_SERVICE_DATA.values());
        callback.onLoaded(notes);
      }
    }, SERVICE_LATENCY_IN_MILLIS);
  }

  @Override
  public void getNote(final String noteId, final NotesServiceCallback<Note> callback) {
    //TODO: Add network latency here too.
    Note note = NOTES_SERVICE_DATA.get(noteId);
    callback.onLoaded(note);
  }

  @Override
  public void saveNote(Note note) {
    NOTES_SERVICE_DATA.put(note.getId(), note);
  }

}
