package com.example.ma.sm.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

  private final RealmResults<Portfolio> portfolios;
  private PortfolioFragment.OnListFragmentInteractionListener listener;
  private Context context;

  public PortfolioAdapter(Context context, RealmResults<Portfolio> portfolios, PortfolioFragment.OnListFragmentInteractionListener listener) {
    this.portfolios = portfolios;
    portfolios.addChangeListener(this);
    this.listener = listener;
    this.context = context;
  }


  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_portfolio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final Portfolio portfolio = portfolios.get(position);
    TypedArray images = context.getResources().obtainTypedArray(R.array.portfolio_images);
    int photoId = 1 + (int) (portfolio.getId() % 4);
    holder.image.setImageResource(images.getResourceId(photoId, R.drawable.photo1));
    images.recycle();
    holder.content.setText(portfolio.getName());
    holder.content.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onListFragmentInteraction(portfolio, holder.image);
      }
    });
  }

  @Override
  public int getItemCount() {
    return portfolios.size();
  }

  @Override
  public void onChange(Object element) {
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.portfolio_image)
    ImageView image;

    @BindView(R.id.portfolio_item)
    TextView content;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }
}
