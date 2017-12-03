package com.example.ma.sm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;
import com.example.ma.sm.adapter.PortfolioAdapter;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.task.listeners.OnCancellableListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class PortfolioFragment extends BaseFragment implements OnCancellableListener {

  public static final String ARG_COLUMN_COUNT = "column-count";
  @BindView(R.id.list)
  RecyclerView recyclerView;
  @BindView(R.id.progressBar)
  ProgressBar progressBar;
  @Inject
  Realm realm;
  private int mColumnCount = 1;
  private OnListFragmentInteractionListener listener;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public PortfolioFragment() {
  }

  @SuppressWarnings("unused")
  public static Fragment newInstance(int columnCount) {
    PortfolioFragment fragment = new PortfolioFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_COLUMN_COUNT, columnCount);
    fragment.setArguments(args);
    Timber.v("newInstance");
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
    }
    StockApp.get().injector().inject(this);
    Timber.v("onCreate");
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_portfolio_list,
        container, false);
    ButterKnife.setDebug(true);
    ButterKnife.bind(this, view);
    Timber.v("onCreateView");
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (mColumnCount <= 1) {
      recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), mColumnCount));
    }
    recyclerView.setAdapter(new PortfolioAdapter(getContext(), realm.where(Portfolio.class).findAll(), listener));
    Timber.v("onViewCreated");
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      listener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnListFragmentInteractionListener");
    }
    Timber.v("onAttach");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    realm.close();
    Timber.v("onDestroy");
  }

  @Override
  public void cancel() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBar.setVisibility(View.GONE);
      }
    });
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnListFragmentInteractionListener {
    void onListFragmentInteraction(Portfolio item, ImageView iv);
  }

}
