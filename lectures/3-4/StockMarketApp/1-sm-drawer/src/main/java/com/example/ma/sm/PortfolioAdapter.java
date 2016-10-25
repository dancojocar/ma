package com.example.ma.sm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ma.sm.fragments.PortfolioFragment.OnListFragmentInteractionListener;
import com.example.ma.sm.model.Portfolio;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Portfolio} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

  private final List<Portfolio> mValues;
  private final OnListFragmentInteractionListener mListener;

  public PortfolioAdapter(List<Portfolio> items, OnListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_portfolio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mContentView.setText(mValues.get(position).getName());

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onListFragmentInteraction(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void remove(int[] reverseSortedPositions) {
    for (int i : reverseSortedPositions)
      mValues.remove(i);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mContentView;
    public Portfolio mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mContentView = (TextView) view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}
