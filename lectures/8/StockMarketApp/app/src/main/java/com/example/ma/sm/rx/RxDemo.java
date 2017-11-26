package com.example.ma.sm.rx;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RxDemo {

  public void start() {
    helloWorld();
    fromOperator();
    fetch();
  }

  private void helloWorld() {
    Observable<String> myObservable
        = Observable.just("Hello"); // Emits "Hello"

    Observer<String> myObserver = new Observer<String>() {
      @Override
      public void onComplete() {
        // Called when the observable has no more data to emit
      }

      @Override
      public void onError(Throwable e) {
        Timber.e(e, "Unknown error");
      }

      @Override
      public void onSubscribe(Disposable d) {
        Timber.d("Subscribed");
      }

      @Override
      public void onNext(String s) {
        // Called each time the observable emits data
        Timber.d("MY OBSERVER %s", s);
      }
    };

    myObservable.subscribe(myObserver);

  }


  private void fromOperator() {
    Observable<Integer> myArrayObservable
        = Observable.fromArray(1, 2, 3, 4, 5, 6); // Emits each item of the array, one at a time

    myArrayObservable = myArrayObservable.map(new Function<Integer, Integer>() { // Input and Output are both Integer
      @Override
      public Integer apply(Integer integer) {
        return integer * integer; // Square the number
      }
    });

    myArrayObservable = myArrayObservable
        .skip(2) // Skip the first two items
        .filter(new Predicate<Integer>() {
          @Override
          public boolean test(Integer integer) throws Exception {
            return integer % 2 == 0; // Ignores any item that returns false
          }

        });

    myArrayObservable.subscribe(new Consumer<Integer>() {
      @Override
      public void accept(Integer integer) throws Exception {
        Timber.d("From Action %s", String.valueOf(integer)); // Prints the number received
      }
    });
  }


  private void fetch() {
    Observable<String> fetchFromGoogle = Observable.create(new ObservableOnSubscribe<String>() {
      @Override
      public void subscribe(ObservableEmitter<String> emitter) throws Exception {
        try {
          String data = fetchData("http://www.google.com");
          emitter.onNext(data); // Emit the contents of the URL
          emitter.onComplete(); // Nothing more to emit
        } catch (Exception e) {
          emitter.onError(e); // In case there are network errors
        }
      }
    });


    Observable<String> fetchFromYahoo = Observable.create(new ObservableOnSubscribe<String>() {
      @Override
      public void subscribe(ObservableEmitter<String> emitter) throws Exception {
        try {
          String data = fetchData("http://www.yahoo.com");
          emitter.onNext(data); // Emit the contents of the URL
          emitter.onComplete(); // Nothing more to emit
        } catch (Exception e) {
          emitter.onError(e); // In case there are network errors
        }
      }
    });


    // Fetch from both simultaneously
    Observable<String> zipped
        = Observable.zip(fetchFromGoogle, fetchFromYahoo, new BiFunction<String, String, String>() {
      @Override
      public String apply(String google, String yahoo) {
        // Do something with the results of both threads
        return google + "\n" + yahoo;
      }
    });

    zipped.subscribeOn(Schedulers.newThread()) // Create a new Thread
        .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
        .subscribe(new Consumer<String>() {
          @Override
          public void accept(String s) throws Exception {
            Timber.v("received from google and yahoo: %s ", s);
          }
        });

  }

  private String fetchData(String s) {
    Random rand = new Random();
    int sec = rand.nextInt(5);
    try {
      Timber.v("%s is sleeping %s sec", s, sec);
      Thread.sleep(sec * 1000);
    } catch (InterruptedException e) {
      Timber.e(e, "Interrupted while sleeping :(");
    }
    return s;
  }
}
