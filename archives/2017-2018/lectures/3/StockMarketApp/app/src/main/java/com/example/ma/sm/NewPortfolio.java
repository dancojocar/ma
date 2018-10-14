package com.example.ma.sm;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class NewPortfolio extends Dialog {
  private static final String TAG = NewPortfolio.class.getSimpleName();

  NewPortfolio(Context context, final StockApp app) {
    super(context);
    setContentView(R.layout.new_portfolio);
    setTitle("New Portfolio");

    final EditText et = findViewById(R.id.portfolioName);
    Button ok = findViewById(R.id.portfolioCreate);
    Button cancel = findViewById(R.id.portfolioCancel);

    ok.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String portfolioName = et.getText().toString();
        Log.v(TAG, "onClick: " + portfolioName);
        app.getManager().addPortfolio(portfolioName);
        dismiss();
      }
    });
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.v(TAG, "cancel onClick");
        dismiss();
      }
    });
    show();
  }

}
