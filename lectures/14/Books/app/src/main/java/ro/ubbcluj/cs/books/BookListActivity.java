package ro.ubbcluj.cs.books;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

  @BindView(R.id.progress)
  ProgressBar progressBar;

  @BindView(R.id.fab)
  FloatingActionButton fab;
  private View recyclerView;
  private Manager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_list);
    ButterKnife.bind(this);
    manager = new Manager();

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setTitle(getTitle());

    recyclerView = findViewById(R.id.event_list);
    assert recyclerView != null;
    setupRecyclerView((RecyclerView) recyclerView);
    loadEvents();

    if (manager.networkConnectivity(this)) {
      Observable.interval(10, TimeUnit.SECONDS)
          .timeInterval()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Subscriber<TimeInterval<Long>>() {
            @Override
            public void onCompleted() {
              Timber.v("Refresh data complete");
            }

            @Override
            public void onError(Throwable e) {
              Timber.e(e, "Error refresh data");
              unsubscribe();
            }

            @Override
            public void onNext(TimeInterval<Long> longTimeInterval) {
              Timber.v("Refresh data");
              if (!loadEvents())
                unsubscribe();
            }
          });
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    loadEvents();
  }

  private boolean loadEvents() {
    boolean conectivity = manager.networkConnectivity(getApplicationContext());
    if (conectivity) {
      fab.setVisibility(View.VISIBLE);
    } else {
      fab.setVisibility(View.GONE);
      showError("No internet connection!");
    }
    manager.loadEvents(progressBar, this);
    return conectivity;
  }

  @Override
  public void add(Book book) {
    adapter.addData(book);

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


  @OnClick(R.id.fab)
  public void onFabClick(View view) {
    Intent intent = new Intent(getApplication(), NewBook.class);
    startActivityForResult(intent, 10000);
  }

  private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
    adapter = new MyAdapter();
    recyclerView.setAdapter(adapter);
  }
}
