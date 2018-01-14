package ro.ubbcluj.cs.books;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ro.ubbcluj.cs.books.adapter.MyAdapter;
import ro.ubbcluj.cs.books.books.R;
import ro.ubbcluj.cs.books.domain.Book;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.TimeInterval;
import timber.log.Timber;


/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity implements MyCallback {

  private MyAdapter adapter;

  ProgressBar progressBar;

  FloatingActionButton fab;
  private View recyclerView;
  private Manager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_list);
    manager = new Manager(getApplication());

    fab = findViewById(R.id.fab);
    progressBar = findViewById(R.id.progress);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setTitle(getTitle());

    recyclerView = findViewById(R.id.event_list);
    assert recyclerView != null;
    setupRecyclerView((RecyclerView) recyclerView);
    loadEvents();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Timber.d("Back in main activity");
  }

  private boolean loadEvents() {
    boolean connectivity = manager.networkConnectivity(getApplicationContext());
    if (connectivity) {
      fab.setVisibility(View.VISIBLE);
    } else {
      fab.setVisibility(View.GONE);
      showError("No internet connection!");
    }
    manager.loadEvents(progressBar, this);
    return connectivity;
  }

  @Override
  public void showError(String error) {
    progressBar.setVisibility(View.GONE);
    Snackbar.make(recyclerView, error, Snackbar.LENGTH_INDEFINITE)
        .setAction("RETRY", new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            loadEvents();
          }
        }).show();
  }

  @Override
  public void clear() {
    adapter.clear();
  }


  public void onAddClick(View view) {
    Intent intent = new Intent(getApplication(), NewBook.class);
    startActivityForResult(intent, 10000);
  }

  public void onRefreshClick(View view) {
    manager.loadEvents(progressBar, this);
  }

  private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
    adapter = new MyAdapter();
    ((BookApp) getApplication()).db.getBookDao().getBooks()
        .observe(this, new Observer<List<Book>>() {
          @Override
          public void onChanged(@Nullable List<Book> books) {
            adapter.setData(books);
          }
        });
    recyclerView.setAdapter(adapter);
  }
}
