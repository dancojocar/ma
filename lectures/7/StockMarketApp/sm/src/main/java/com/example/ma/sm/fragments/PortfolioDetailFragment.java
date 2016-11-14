package com.example.ma.sm.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.ma.sm.StockApp;
import com.example.ma.sm.model.Symbol;
import com.example.ma.sm.util.SwipeDismissListViewTouchListener;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.SymbolTable;


public class PortfolioDetailFragment extends ListFragment {

  private ArrayAdapter<String> adapter;
  private RealmResults<Symbol> allSorted;
  @Inject
  Realm realm;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Timber.v("onActivityCreated");
    StockApp.get().injector().inject(this);

    long portfolioId = getArguments().getLong("portfolioId");

    allSorted = realm.where(Symbol.class).
        equalTo(SymbolTable.COLUMN_PORTFOLIO_ID, portfolioId).
        findAllSorted(SymbolTable.COLUMN_NAME);
    List<String> list = new ArrayList<>(allSorted.size());
    for (Symbol symbol : allSorted) {
      list.add(symbol.getName());
    }
    adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_activated_1, list);
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
                  Timber.v("deleting symbolId: %s", symbolID);
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
    Symbol symbol = allSorted.get(position);
    long symbolId = symbol.getId();
    Intent intent = new Intent();
    intent.setClass(getContext(), SymbolDetailsActivity.class);
    intent.putExtra("symbolId", symbolId);
    startActivity(intent);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    realm.close();
    RefWatcher refWatcher = StockApp.getRefWatcher();
    refWatcher.watch(this);
  }
}
