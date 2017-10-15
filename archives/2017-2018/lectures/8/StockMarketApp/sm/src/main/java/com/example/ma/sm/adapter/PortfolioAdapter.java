package com.example.ma.sm.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ma.sm.R;
import com.example.ma.sm.fragments.PortfolioFragment;
import com.example.ma.sm.model.Portfolio;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Portfolio}
 */
public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> implements RealmChangeListener {

  private PortfolioFragment.OnListFragmentInteractionListener listener;
  private final RealmResults<Portfolio> portfolios;

  public PortfolioAdapter(RealmResults<Portfolio> portfolios, PortfolioFragment.OnListFragmentInteractionListener listener) {
    this.portfolios = portfolios;
    portfolios.addChangeListener(this);
    this.listener = listener;
  }


  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_portfolio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final Portfolio portfolio = portfolios.get(position);
    holder.content.setText(portfolio.getName());
    holder.content.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onListFragmentInteraction(portfolio);
      }
    });
  }

  @Override
  public int getItemCount() {
    return portfolios.size();
  }

  @Override
  public void onChange() {
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.portfolio_item)
    TextView content;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }
}
