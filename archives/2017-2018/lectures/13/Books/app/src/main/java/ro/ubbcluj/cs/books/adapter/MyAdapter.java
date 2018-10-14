package ro.ubbcluj.cs.books.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ro.ubbcluj.cs.books.EventDetailActivity;
import ro.ubbcluj.cs.books.EventDetailFragment;
import ro.ubbcluj.cs.books.books.R;
import ro.ubbcluj.cs.books.domain.Book;

public class MyAdapter
    extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

  private List<Book> mValues;

  public MyAdapter() {
    mValues = new ArrayList<>();
  }

  public void setData(List<Book> books) {
    mValues = books;
    notifyDataSetChanged();
  }

  public void clear() {
    mValues.clear();
    notifyDataSetChanged();
  }


  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.book_list_content, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mIdView.setText(String.valueOf(mValues.get(position).getId()));
    holder.mContentView.setText(mValues.get(position).getTitle());

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Context context = v.getContext();
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra(EventDetailFragment.ARG_ITEM_ID, String.valueOf(holder.mItem.getId()));

        context.startActivity(intent);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    final View mView;
    final TextView mIdView;
    final TextView mContentView;
    Book mItem;

    ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.id);
      mContentView = view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}
