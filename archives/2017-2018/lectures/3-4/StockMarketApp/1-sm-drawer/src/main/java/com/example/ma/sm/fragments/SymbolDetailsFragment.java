package com.example.ma.sm.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ma.sm.model.Symbol;

public class SymbolDetailsFragment extends Fragment {
  /**
   * Create a new instance of DetailsFragment, initialized to
   * show the text at 'index'.
   */
  public static SymbolDetailsFragment newInstance(Symbol symbol) {
    SymbolDetailsFragment f = new SymbolDetailsFragment();

    // Supply index input as an argument.
    Bundle args = new Bundle();
    args.putSerializable("symbol", symbol);
    f.setArguments(args);

    return f;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Symbol symbol = (Symbol) getArguments().getSerializable("symbol");

    ScrollView scroller = new ScrollView(getActivity());
    LinearLayout ll = new LinearLayout(getActivity());
    ll.setOrientation(LinearLayout.VERTICAL);
    TextView text = new TextView(getActivity());
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        4, getActivity().getResources().getDisplayMetrics());
    text.setPadding(padding, padding, padding, padding);
    text.setText("Name: " + symbol.getName());
    ll.addView(text);
    text = new TextView(getActivity());
    text.setPadding(padding, padding, padding, padding);
    text.setText("Quantity: " + symbol.getQuantity());
    ll.addView(text);
    text = new TextView(getActivity());
    text.setPadding(padding, padding, padding, padding);
    text.setText("Price: " + symbol.getAcquisitionPrice());
    ll.addView(text);
    text = new TextView(getActivity());
    text.setPadding(padding, padding, padding, padding);
    text.setText("Value: " + symbol.getAcquisitionPrice() * symbol.getQuantity());
    ll.addView(text);
    scroller.addView(ll);
    return scroller;
  }
}