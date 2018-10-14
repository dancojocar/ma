package com.example.ma.sm.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ma.sm.R;
import com.example.ma.sm.fragments.PortfolioFragment;
import com.example.ma.sm.helper.PortfolioHelper;
import com.example.ma.sm.model.Portfolio;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Portfolio}
 */
public class PortfolioAdapter extends CursorRecyclerViewAdapter<PortfolioAdapter.ViewHolder> {
  private final PortfolioFragment.OnListFragmentInteractionListener listener;

  public PortfolioAdapter(Context context, Cursor cursor, PortfolioFragment.OnListFragmentInteractionListener listener) {
    super(context, cursor);
    this.listener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_portfolio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
    final Portfolio p = PortfolioHelper.fromCursor(cursor);
    if (p != null) {
      holder.content.setText(p.getName());
      holder.view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          listener.onListFragmentInteraction(p);
        }
      });
    }
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View view;
    public final TextView content;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      content = (TextView) view.findViewById(R.id.portfolio_item);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + content.getText() + "'";
    }
  }
}
