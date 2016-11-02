package com.example.ma.sm.adapter;


import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;


public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  private Context context;

  private Cursor cursor;

  private boolean valid;

  private int rowIdColumn;

  private DataSetObserver observer;

  public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
    this.context = context;
    this.cursor = cursor;
    valid = cursor != null;
    rowIdColumn = valid ? this.cursor.getColumnIndex("_id") : -1;
    observer = new NotifyingDataSetObserver();
    if (this.cursor != null) {
      this.cursor.registerDataSetObserver(observer);
    }
  }

  public Cursor getCursor() {
    return cursor;
  }

  @Override
  public int getItemCount() {
    if (valid && cursor != null) {
      return cursor.getCount();
    }
    return 0;
  }

  @Override
  public long getItemId(int position) {
    if (valid && cursor != null && cursor.moveToPosition(position)) {
      return cursor.getLong(rowIdColumn);
    }
    return 0;
  }

  @Override
  public void setHasStableIds(boolean hasStableIds) {
    super.setHasStableIds(true);
  }

  public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

  @Override
  public void onBindViewHolder(VH viewHolder, int position) {
    if (!valid) {
      throw new IllegalStateException("this should only be called when the cursor is valid");
    }
    if (!cursor.moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    onBindViewHolder(viewHolder, cursor);
  }

  /**
   * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
   * closed.
   */
  public void changeCursor(Cursor cursor) {
    Cursor old = swapCursor(cursor);
    if (old != null) {
      old.close();
    }
  }

  /**
   * Swap in a new Cursor, returning the old Cursor.  Unlike
   * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
   * closed.
   */
  public Cursor swapCursor(Cursor newCursor) {
    if (newCursor == cursor) {
      return null;
    }
    final Cursor oldCursor = cursor;
    if (oldCursor != null && observer != null) {
      oldCursor.unregisterDataSetObserver(observer);
    }
    cursor = newCursor;
    if (cursor != null) {
      if (observer != null) {
        cursor.registerDataSetObserver(observer);
      }
      rowIdColumn = newCursor.getColumnIndexOrThrow("_id");
      valid = true;
      notifyDataSetChanged();
    } else {
      rowIdColumn = -1;
      valid = false;
      notifyDataSetChanged();
      //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
    }
    return oldCursor;
  }

  private class NotifyingDataSetObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      super.onChanged();
      valid = true;
      notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      super.onInvalidated();
      valid = false;
      notifyDataSetChanged();
      //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
    }
  }
}