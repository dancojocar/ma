package ro.ubbcluj.cs.books;


import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

import ro.ubbcluj.cs.books.domain.Book;
import ro.ubbcluj.cs.books.service.BookService;
import ro.ubbcluj.cs.books.service.ServiceFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

class Manager {
  private BookApp app;
  private BookService service;

  Manager(Application application) {
    this.app = (BookApp) application;
    service = ServiceFactory.createRetrofitService(BookService.class,
        BookService.SERVICE_ENDPOINT);
  }


  boolean networkConnectivity(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    assert cm != null;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }

  void loadEvents(final ProgressBar progressBar, final MyCallback callback) {

    service.getBooks()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<Book>>() {
          @Override
          public void onCompleted() {
            Timber.v("Book Service completed");
            progressBar.setVisibility(View.GONE);
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e, "Error while loading the events");
            callback.showError("Not able to retrieve the data. Displaying local data!");
          }

          @Override
          public void onNext(final List<Book> books) {
            new Thread(new Runnable() {
              public void run() {
                app.db.getBookDao().deleteBooks();
                app.db.getBookDao().addBooks(books);
              }
            }).start();
            Timber.v("Books persisted");
          }
        });
  }

  void save(final Book book, final MyCallback callback) {
    service.addBook(book)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Book>() {
          @Override
          public void onCompleted() {
            addDataLocally(book);
            Timber.v("Book Service completed");
            callback.clear();
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e, "Error while persisting an book");
            callback.showError("Not able to connect to the server, will not persist!");
          }

          @Override
          public void onNext(Book book) {
            Timber.v("Book persisted");
          }
        });
  }

  private void addDataLocally(final Book book) {
    new Thread(new Runnable() {
      public void run() {
        app.db.getBookDao().addBook(book);
      }
    }).start();
  }
}
