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

package com.example.android.testing.notes.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.testing.notes.Injection;
import com.example.android.testing.notes.R;
import com.example.android.testing.notes.addnote.AddNoteActivity;
import com.example.android.testing.notes.data.Note;
import com.example.android.testing.notes.notedetail.NoteDetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Note}s
 */
public class NotesFragment extends Fragment implements NotesContract.View {

  private static final int REQUEST_ADD_NOTE = 1;

  private NotesContract.UserActionsListener mActionsListener;
  /**
   * Listener for clicks on notes in the RecyclerView.
   */
  NoteItemListener mItemListener = new NoteItemListener() {
    @Override
    public void onNoteClick(Note clickedNote) {
      mActionsListener.openNoteDetails(clickedNote);
    }
  };
  private NotesAdapter mListAdapter;

  public NotesFragment() {
    // Requires empty public constructor
  }

  public static NotesFragment newInstance() {
    return new NotesFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mListAdapter = new NotesAdapter(new ArrayList<Note>(0), mItemListener);
    mActionsListener = new NotesPresenter(Injection.provideNotesRepository(), this);
  }

  @Override
  public void onResume() {
    super.onResume();
    mActionsListener.loadNotes(false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setRetainInstance(true);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // If a note was successfully added, show snackbar
    if (REQUEST_ADD_NOTE == requestCode && Activity.RESULT_OK == resultCode) {
      View view = getView();
      if (view != null) {
        Snackbar.make(view, getString(R.string.successfully_saved_note_message), Snackbar.LENGTH_SHORT).show();
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_notes, container, false);
    RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.notes_list);
    recyclerView.setAdapter(mListAdapter);

    Context context = getContext();
    if (context != null) {
      Resources resources = context.getResources();
      if (resources != null) {
        int numColumns = resources.getInteger(R.integer.num_notes_columns);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context, numColumns));
      }
    }

    // Set up floating action button
    FragmentActivity activity = getActivity();
    if (activity != null) {
      FloatingActionButton fab =
          (FloatingActionButton) activity.findViewById(R.id.fab_add_notes);

      fab.setImageResource(R.drawable.ic_add);
      fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mActionsListener.addNewNote();
        }
      });

      // Pull-to-refresh
      SwipeRefreshLayout swipeRefreshLayout =
          (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
      swipeRefreshLayout.setColorSchemeColors(
          ContextCompat.getColor(activity, R.color.colorPrimary),
          ContextCompat.getColor(activity, R.color.colorAccent),
          ContextCompat.getColor(activity, R.color.colorPrimaryDark));
      swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
          mActionsListener.loadNotes(true);
        }
      });
    }
    return root;
  }

  @Override
  public void setProgressIndicator(final boolean active) {

    if (getView() == null) {
      return;
    }
    final SwipeRefreshLayout srl =
        (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

    // Make sure setRefreshing() is called after the layout is done with everything else.
    srl.post(new Runnable() {
      @Override
      public void run() {
        srl.setRefreshing(active);
      }
    });
  }

  @Override
  public void showNotes(List<Note> notes) {
    mListAdapter.replaceData(notes);
  }

  @Override
  public void showAddNote() {
    Intent intent = new Intent(getContext(), AddNoteActivity.class);
    startActivityForResult(intent, REQUEST_ADD_NOTE);
  }

  @Override
  public void showNoteDetailUi(String noteId) {
    // in it's own Activity, since it makes more sense that way and it gives us the flexibility
    // to show some Intent stubbing.
    Intent intent = new Intent(getContext(), NoteDetailActivity.class);
    intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteId);
    startActivity(intent);
  }


  public interface NoteItemListener {

    void onNoteClick(Note clickedNote);
  }

  private static class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private List<Note> mNotes;
    private final NoteItemListener mItemListener;

    public NotesAdapter(List<Note> notes, NoteItemListener itemListener) {
      setList(notes);
      mItemListener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      Context context = parent.getContext();
      LayoutInflater inflater = LayoutInflater.from(context);
      View noteView = inflater.inflate(R.layout.item_note, parent, false);

      return new ViewHolder(noteView, mItemListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
      Note note = mNotes.get(position);

      viewHolder.title.setText(note.getTitle());
      viewHolder.description.setText(note.getDescription());
    }

    public void replaceData(List<Note> notes) {
      setList(notes);
      notifyDataSetChanged();
    }

    private void setList(List<Note> notes) {
      mNotes = checkNotNull(notes);
    }

    @Override
    public int getItemCount() {
      return mNotes.size();
    }

    public Note getItem(int position) {
      return mNotes.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      public TextView title;

      public TextView description;
      private final NoteItemListener mItemListener;

      public ViewHolder(View itemView, NoteItemListener listener) {
        super(itemView);
        mItemListener = listener;
        title = (TextView) itemView.findViewById(R.id.note_detail_title);
        description = (TextView) itemView.findViewById(R.id.note_detail_description);
        itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
        int position = getAdapterPosition();
        Note note = getItem(position);
        mItemListener.onNoteClick(note);

      }
    }
  }

}
