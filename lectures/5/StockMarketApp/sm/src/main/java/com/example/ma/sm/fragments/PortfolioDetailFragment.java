package com.example.ma.sm.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.ma.sm.database.DBContract;
import com.example.ma.sm.provider.SymbolContentProvider;
import com.example.ma.sm.util.SwipeDismissListViewTouchListener;

import static com.example.ma.sm.database.DBContract.SymbolTable;


public class PortfolioDetailFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = PortfolioDetailFragment.class.getSimpleName();

  private long portfolioId;
  private SimpleCursorAdapter adapter;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.v(TAG, "onActivityCreated");

    portfolioId = getArguments().getLong("portfolioId");

    String[] mFromColumns = {
        DBContract.SymbolTable.COLUMN_NAME
    };
    int[] mToFields = {
        android.R.id.text1
    };

    getLoaderManager().initLoader(0, null, this);
    adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, null, mFromColumns, mToFields, 0);
    setListAdapter(adapter);

    SwipeDismissListViewTouchListener touchListener =
        new SwipeDismissListViewTouchListener(
            getListView(),
            new SwipeDismissListViewTouchListener.DismissCallbacks() {
              @Override
              public boolean canDismiss(int position) {
                return true;
              }

              @Override
              public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                Cursor cursor = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
                for (int position : reverseSortedPositions) {
                  cursor.moveToPosition(position);
                  String symbolID = cursor.getString(cursor.getColumnIndex(SymbolTable._ID));
                  Log.v(TAG, "deleting symbolId: " + symbolID);
                }
                adapter.notifyDataSetChanged();
              }
            });
    getListView().setOnTouchListener(touchListener);
    // Setting this scroll listener is required to ensure that during ListView scrolling,
    // we don't look for swipes.
    getListView().setOnScrollListener(touchListener.makeScrollListener());
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onListItemClick(ListView listView, View v, int position, long id) {
    Cursor cursor = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
    cursor.moveToPosition(position);
    long symbolId = Long.parseLong(cursor.getString(cursor.getColumnIndex(SymbolTable._ID)));
    Intent intent = new Intent();
    intent.setClass(getContext(), SymbolDetailsActivity.class);
    intent.putExtra("symbolId", symbolId);
    startActivity(intent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    String[] projection = {SymbolTable._ID, SymbolTable.COLUMN_NAME};
    String selection = SymbolTable.COLUMN_PORTFOLIO_ID + " = ? ";
    String[] selectionArgs = new String[]{String.valueOf(portfolioId)};
    String order = SymbolTable.COLUMN_NAME + " ASC ";
    return new CursorLoader(this.getContext(), SymbolContentProvider.CONTENT_URI, projection, selection, selectionArgs, order);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    adapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    adapter.swapCursor(null);
  }
}
