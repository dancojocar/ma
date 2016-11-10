package com.example.ma.sm.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;

import timber.log.Timber;

public class NewPortfolio extends Dialog {

  public NewPortfolio(Context context, final StockApp app) {
    super(context);
    setContentView(R.layout.new_portfolio);
    setTitle("New Portfolio");

    final EditText et = (EditText) findViewById(R.id.portfolioName);
    Button ok = (Button) findViewById(R.id.portfolioCreate);
    Button cancel = (Button) findViewById(R.id.portfolioCancel);

    ok.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String portfolioName = et.getText().toString();
        Timber.v("onClick: %s", portfolioName);
        app.getManager().addPortfolio(portfolioName);
        dismiss();
      }
    });
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Timber.v("cancel onClick");
        dismiss();
      }
    });
    show();
  }

}
