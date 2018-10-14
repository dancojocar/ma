package com.example.ma.sm.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma.sm.net.StockRestConnection;
import com.example.ma.sm.provider.SymbolContentProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import static com.example.ma.sm.database.DBContract.SymbolTable;

public class SymbolDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String TAG = StockRestConnection.class.getSimpleName();

  private long symbolId;
  private TextView id;
  private TextView name;
  private TextView quantity;
  private TextView price;
  private TextView date;
  private TextView value;
  private TextView portfolioId;


  @Override
  public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                           Bundle savedInstanceState) {
    symbolId = getArguments().getLong("symbolId");

    getLoaderManager().initLoader(0, null, this);

    ScrollView scroller = new ScrollView(getActivity());
    LinearLayout ll = new LinearLayout(getActivity());
    ll.setOrientation(LinearLayout.VERTICAL);
    name = new TextView(getActivity());
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        4, getActivity().getResources().getDisplayMetrics());
    name.setPadding(padding, padding, padding, padding);
    ll.addView(name);
    quantity = new TextView(getActivity());
    quantity.setPadding(padding, padding, padding, padding);
    ll.addView(quantity);
    price = new TextView(getActivity());
    price.setPadding(padding, padding, padding, padding);
    ll.addView(price);
    date = new TextView(getActivity());
    date.setPadding(padding, padding, padding, padding);
    ll.addView(date);
    value = new TextView(getActivity());
    value.setPadding(padding, padding, padding, padding);
    ll.addView(value);
    id = new TextView(getActivity());
    id.setPadding(padding, padding, padding, padding);
    ll.addView(id);
    portfolioId = new TextView(getActivity());
    portfolioId.setPadding(padding, padding, padding, padding);
    ll.addView(portfolioId);
    LinearLayout hl = new LinearLayout(getActivity());
    hl.setOrientation(LinearLayout.HORIZONTAL);
    TextView tv = new TextView(getActivity());
    tv.setPadding(padding, padding, padding, padding);
    tv.setText("File name: ");
    hl.addView(tv);
    final EditText ed = new EditText(getActivity());
    ed.setPadding(padding, padding, padding, padding);
    ed.setText("sec" + symbolId + ".txt");
    hl.addView(ed);
    Button save = new Button(getActivity());
    save.setPadding(padding, padding, padding, padding);
    save.setText("Save File");
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String filename = ed.getText().toString();
        String string = String.format("name: %s\nquantity: %s\nprice: %s\n", name, quantity, price);
        FileOutputStream outputStream;
        try {
          outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
          outputStream.write(string.getBytes());
          outputStream.close();
          Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          Log.e(TAG, "error saving the file: " + filename, e);
        }
      }
    });
    hl.addView(save);
    Button saveCache = new Button(getActivity());
    saveCache.setPadding(padding, padding, padding, padding);
    saveCache.setText("Save CacheFile");
    saveCache.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String filename = ed.getText().toString();
        String string = String.format("name: %s\nquantity: %s\nprice: %s\n", name, quantity, price);
        FileOutputStream outputStream;
        try {
          outputStream = new FileOutputStream(new File(getContext().getCacheDir(), filename));
          outputStream.write(string.getBytes());
          outputStream.close();
          Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          Log.e(TAG, "error saving the file: " + filename, e);
        }
      }
    });
    hl.addView(saveCache);
    ll.addView(hl);
    scroller.addView(ll);
    return scroller;
  }


  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] projection = {
        SymbolTable._ID,
        SymbolTable.COLUMN_NAME,
        SymbolTable.COLUMN_PRICE,
        SymbolTable.COLUMN_QUANTITY,
        SymbolTable.COLUMN_DATE,
        SymbolTable.COLUMN_PORTFOLIO_ID};
    String selection = SymbolTable._ID + " = ? ";
    String[] selectionArgs = new String[]{String.valueOf(symbolId)};
    String order = SymbolTable.COLUMN_NAME + " ASC ";
    return new CursorLoader(this.getContext(), SymbolContentProvider.CONTENT_URI, projection, selection, selectionArgs, order);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    cursor.moveToFirst();
    long symbolId = cursor.getLong(cursor.getColumnIndex(SymbolTable._ID));
    String symbolName = cursor.getString(cursor.getColumnIndex(SymbolTable.COLUMN_NAME));
    double symbolPrice = cursor.getDouble(cursor.getColumnIndex(SymbolTable.COLUMN_PRICE));
    long symbolQuantity = cursor.getLong(cursor.getColumnIndex(SymbolTable.COLUMN_QUANTITY));
    Date symbolDate = new Date(cursor.getLong(cursor.getColumnIndex(SymbolTable.COLUMN_DATE)));
    long pId = cursor.getLong(cursor.getColumnIndex(SymbolTable.COLUMN_PORTFOLIO_ID));
    id.setText("Id: " + symbolId);
    name.setText("Name: " + symbolName);
    price.setText("Price: " + symbolPrice);
    quantity.setText("Quantity: " + symbolQuantity);
    date.setText("Date: " + symbolDate);
    value.setText("Value: " + symbolQuantity * symbolPrice);
    portfolioId.setText("PortfolioId: " + pId);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }
}