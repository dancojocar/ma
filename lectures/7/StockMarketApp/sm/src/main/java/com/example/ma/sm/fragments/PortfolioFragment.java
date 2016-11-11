package com.example.ma.sm.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;
import com.example.ma.sm.adapter.PortfolioAdapter;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.provider.PortfolioContentProvider;
import com.example.ma.sm.task.listeners.OnCancellableListener;

import timber.log.Timber;

import static com.example.ma.sm.database.DBContract.PortfolioTable;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class PortfolioFragment extends BaseFragment implements OnCancellableListener, LoaderManager.LoaderCallbacks<Cursor> {

  public static final String ARG_COLUMN_COUNT = "column-count";
  private StockApp app;
  private int mColumnCount = 1;
  private OnListFragmentInteractionListener listener;
  private RecyclerView recyclerView;
  private PortfolioAdapter adapter;
  private ProgressBar progressBar;

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
    Timber.v("onCreate");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_portfolio_list, container, false);
    if (view != null) {
      View innerView = view.findViewById(R.id.list);
      // Set the adapter
      if (innerView instanceof RecyclerView) {
        recyclerView = (RecyclerView) innerView;
      }
    }
    if (recyclerView != null) {
      if (mColumnCount <= 1) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
      } else {
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), mColumnCount));
      }
      if (adapter == null) {
        adapter = new PortfolioAdapter(this.getContext(), app.getManager().getPortfolios(), listener);
      }
      recyclerView.setAdapter(adapter);
    }
    if (view != null)
      progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

    Timber.v("onCreateView");
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
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
    app = (StockApp) context.getApplicationContext();
    Timber.v("onAttach");
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Timber.v("onActivityCreated");
  }

  @Override
  public void onStart() {
    super.onStart();
    loadData();
    Timber.v("onStart");
  }

  private void loadData() {
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public void onResume() {
    super.onResume();
    Timber.v("onResume");
  }

  @Override
  public void onPause() {
    super.onPause();
    Timber.v("onPause");
  }

  @Override
  public void onStop() {
    super.onStop();
    Timber.v("onStop");
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Timber.v("onSaveInstanceState");
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
    Timber.v("onDetach");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Timber.v("onDestroyView");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    adapter.swapCursor(null);
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

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] projection = {PortfolioTable._ID, PortfolioTable.COLUMN_NAME, PortfolioTable.COLUMN_LAST_MODIFIED};
    return new CursorLoader(this.getContext(), PortfolioContentProvider.CONTENT_URI, projection, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (adapter != null)
      adapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    adapter.swapCursor(null);
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
    void onListFragmentInteraction(Portfolio item);
  }

}
