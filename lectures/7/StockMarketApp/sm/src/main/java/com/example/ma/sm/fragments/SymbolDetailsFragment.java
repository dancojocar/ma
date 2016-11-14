package com.example.ma.sm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;
import com.example.ma.sm.model.Symbol;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import timber.log.Timber;

public class SymbolDetailsFragment extends BaseFragment {

  private DecimalFormat df = new DecimalFormat("##.##");

  private long symbolId;
  @BindView(R.id.symbol_name)
  TextView name;
  @BindView(R.id.symbol_quantity)
  TextView quantity;
  @BindView(R.id.symbol_price)
  TextView price;
  @BindView(R.id.symbol_date)
  TextView date;
  @BindView(R.id.symbol_value)
  TextView value;
  @BindView(R.id.file_name)
  EditText ed;
  @Inject
  Realm realm;


  @Override
  public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.detailed_symbol, container, false);
    StockApp.get().injector().inject(this);
    ButterKnife.bind(this, view);
    symbolId = getArguments().getLong("symbolId");
    return view;
  }


  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Symbol symbol = realm.where(Symbol.class).equalTo("id", symbolId).findFirst();
    if (symbol != null) {
      name.setText(symbol.getName());
      long quantity = symbol.getQuantity();
      this.quantity.setText(String.valueOf(quantity));
      double acquisitionPrice = symbol.getAcquisitionPrice();
      price.setText(df.format(acquisitionPrice));
      date.setText(symbol.getAcquisitionDate().toString());
      value.setText(df.format((symbol.getAcquisitionPrice() * symbol.getQuantity())));
    }
  }

  @OnClick(R.id.save_file)
  public void saveFile(Button saveButton) {
    String filename = ed.getText().toString();
    String string = String.format("name: %s\nquantity: %s\nprice: %s\n", name, quantity, price);
    FileOutputStream outputStream;
    try {
      outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
      outputStream.write(string.getBytes());
      outputStream.close();
      Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Timber.v(e, "error saving the file: %s", filename);
    }
  }

  @OnClick(R.id.save_cache)
  public void saveCache(Button saveCache) {
    String filename = ed.getText().toString();
    String string = String.format("name: %s\nquantity: %s\nprice: %s\n", name, quantity, price);
    FileOutputStream outputStream;
    try {
      outputStream = new FileOutputStream(new File(getContext().getCacheDir(), filename));
      outputStream.write(string.getBytes());
      outputStream.close();
      Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Timber.e(e, "error saving the file: %s", filename);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    realm.close();
  }
}