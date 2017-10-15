package ro.ubbcluj.cs.books;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.ubbcluj.cs.books.domain.Book;
import ro.ubbcluj.cs.books.service.BookService;
import ro.ubbcluj.cs.books.service.ServiceFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class Manager {


  private BookService service;
  private Realm realm = Realm.getDefaultInstance();

  public Manager() {
    service = ServiceFactory.createRetrofitService(BookService.class, BookService.SERVICE_ENDPOINT);
  }

  public boolean networkConnectivity(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }


  public void loadEvents( final ProgressBar progressBar, final MyCallback callback) {

    service.getBooks()
        .timeout(5, TimeUnit.SECONDS)
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
            callback.clear();
            realm.executeTransaction(new Realm.Transaction() {
              @Override
              public void execute(Realm realm) {
                RealmResults<Book> result = realm.where(Book.class).findAll();
                List<Book> books = realm.copyFromRealm(result);
                for (Book book : books) {
                  callback.add(book);
                }
              }
            });
            callback.showError("Not able to retrieve the data. Displaying local data!");
          }

          @Override
          public void onNext(final List<Book> books) {
            callback.clear();
            for (Book book : books)
              callback.add(book);
            realm.executeTransactionAsync(new Realm.Transaction() {
              @Override
              public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(books);
                Timber.v("Books persisted");
              }
            });
          }
        });
    ;
  }

  public void save(Book book) {
    service.addBook(book)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Book>() {
          @Override
          public void onCompleted() {
            Timber.v("Book Service completed");
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e, "Error while persisting an book");
          }

          @Override
          public void onNext(Book book) {
            Timber.v("Book persisted");
          }
        });
  }
}
