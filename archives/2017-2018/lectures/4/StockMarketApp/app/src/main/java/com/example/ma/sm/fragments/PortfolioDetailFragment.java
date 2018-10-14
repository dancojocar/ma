package com.example.ma.sm.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.util.SwipeDismissListViewTouchListener;

import static com.example.ma.sm.fragments.SymbolDetailsFragment.SYMBOL;


public class PortfolioDetailFragment extends ListFragment {
  private static final String TAG = PortfolioDetailFragment.class.getSimpleName();
  public static final String PORTFOLIO = "portfolio";

  private Portfolio portfolio;
  private ArrayAdapter<String> adapter;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    portfolio = (Portfolio) getArguments().getSerializable(PORTFOLIO);
    // Populate list with our static array of titles.
    adapter = new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_list_item_activated_1, portfolio.getSymbolNames());
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
                for (int position : reverseSortedPositions) {
                  adapter.remove(adapter.getItem(position));
                  Log.d(TAG, "Delete item on position: " + position);
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
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent intent = new Intent();
    intent.setClass(getActivity(), SymbolDetailsActivity.class);
    intent.putExtra(SYMBOL, portfolio.getSymbol(position));
    startActivity(intent);
  }
}
