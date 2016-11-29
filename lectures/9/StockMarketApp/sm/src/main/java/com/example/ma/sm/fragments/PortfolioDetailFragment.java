package com.example.ma.sm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ma.sm.StockApp;
import com.example.ma.sm.model.Symbol;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.SymbolTable;


public class PortfolioDetailFragment extends ListFragment {

  @Inject
  Realm realm;
  private RealmResults<Symbol> allSorted;

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
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_activated_1, list);
    setListAdapter(adapter);
    int color = ContextCompat.getColor(getContext(), android.R.color.white);
    getListView().setBackgroundColor(color);
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
    allSorted = null;
    realm.close();
    RefWatcher refWatcher = StockApp.getRefWatcher();
    refWatcher.watch(this);
  }
}
