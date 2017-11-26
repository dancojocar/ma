package com.example.ma.sm.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.example.ma.sm.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PortfolioDetailActivity extends BaseActivity
    implements NewSymbolFragment.OnListFragmentInteractionListener {

  @BindView(R.id.symbol_fab)
  FloatingActionButton fab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.symbols);
    ButterKnife.bind(this);
    fab.setVisibility(View.VISIBLE);
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      // If the screen is now in landscape mode, we can show the
      // dialog in-line with the list so we don't need this activity.
      finish();
      return;
    }

    if (savedInstanceState == null) {
      // During initial setup, plug in the details fragment.
      PortfolioDetailFragment details = new PortfolioDetailFragment();
      details.setArguments(getIntent().getExtras());
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.symbol_content, details)
          .commit();
    }
  }

  @OnClick(R.id.symbol_fab)
  public void newSymbol() {
    fab.setVisibility(View.GONE);
    BaseFragment fragment = new NewSymbolFragment();
    fragment.setArguments(getIntent().getExtras());
    String tag = fragment.getClass().getSimpleName();
    getSupportFragmentManager()
        .beginTransaction()
        .addToBackStack(tag)
        .add(R.id.symbol_content, fragment, tag)
        .commit();
  }

  @Override
  public void onListFragmentInteraction(long portfolioId) {
    Bundle bundle = new Bundle();
    bundle.putLong("portfolioId", portfolioId);
    PortfolioDetailFragment details = new PortfolioDetailFragment();
    details.setArguments(bundle);
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.symbol_content, details)
        .commit();
  }
}