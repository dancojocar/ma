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

import com.example.ma.sm.R;
import com.example.ma.sm.model.Symbol;

public class SymbolDetailsFragment extends Fragment {

  public static final String SYMBOL = "symbol";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Symbol symbol = (Symbol) getArguments().getSerializable(SYMBOL);
    ScrollView scroller = new ScrollView(getActivity());
    if (symbol != null) {
      LinearLayout ll = new LinearLayout(getActivity());
      ll.setOrientation(LinearLayout.VERTICAL);
      TextView text = new TextView(getActivity());
      int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
          4, getActivity().getResources().getDisplayMetrics());
      text.setPadding(padding, padding, padding, padding);
      text.setText(getString(R.string.symbolName, symbol.getName()));
      ll.addView(text);
      text = new TextView(getActivity());
      text.setPadding(padding, padding, padding, padding);
      text.setText(getString(R.string.symbolQuantity, symbol.getQuantity()));
      ll.addView(text);
      text = new TextView(getActivity());
      text.setPadding(padding, padding, padding, padding);
      text.setText(getString(R.string.symbolPrice, symbol.getAcquisitionPrice()));
      ll.addView(text);
      text = new TextView(getActivity());
      text.setPadding(padding, padding, padding, padding);
      text.setText(getString(R.string.symbolValue, symbol.getAcquisitionPrice() * symbol.getQuantity()));
      ll.addView(text);
      scroller.addView(ll);
    } else {
      TextView errorMessage = new TextView(getActivity());
      errorMessage.setText(R.string.symbolErrorMessage);
      scroller.addView(errorMessage);
    }
    return scroller;
  }
}