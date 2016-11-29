package com.example.ma.sm.rx;

import android.view.View;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;

import java.util.Random;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
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
      public void onCompleted() {
        // Called when the observable has no more data to emit
      }

      @Override
      public void onError(Throwable e) {
        Timber.e(e, "Unknown error");
      }

      @Override
      public void onNext(String s) {
        // Called each time the observable emits data
        Timber.d("MY OBSERVER %s", s);
      }
    };

    Subscription mySubscription = myObservable.subscribe(myObserver);

    mySubscription.unsubscribe();
  }


  private void fromOperator() {
    Observable<Integer> myArrayObservable
        = Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}); // Emits each item of the array, one at a time

    myArrayObservable = myArrayObservable.map(new Func1<Integer, Integer>() { // Input and Output are both Integer
      @Override
      public Integer call(Integer integer) {
        return integer * integer; // Square the number
      }
    });

    myArrayObservable = myArrayObservable
        .skip(2) // Skip the first two items
        .filter(new Func1<Integer, Boolean>() {
          @Override
          public Boolean call(Integer integer) { // Ignores any item that returns false
            return integer % 2 == 0;
          }
        });

    myArrayObservable.subscribe(new Action1<Integer>() {
      @Override
      public void call(Integer i) {
        Timber.d("From Action %s", String.valueOf(i)); // Prints the number received
      }
    });
  }


  private void fetch() {
    Observable<String> fetchFromGoogle = Observable.create(new Observable.OnSubscribe<String>() {
      @Override
      public void call(Subscriber<? super String> subscriber) {
        try {
          String data = fetchData("http://www.google.com");
          subscriber.onNext(data); // Emit the contents of the URL
          subscriber.onCompleted(); // Nothing more to emit
        } catch (Exception e) {
          subscriber.onError(e); // In case there are network errors
        }
      }
    });


    Observable<String> fetchFromYahoo = Observable.create(new Observable.OnSubscribe<String>() {
      @Override
      public void call(Subscriber<? super String> subscriber) {
        try {
          String data = fetchData("http://www.yahoo.com");
          subscriber.onNext(data); // Emit the contents of the URL
          subscriber.onCompleted(); // Nothing more to emit
        } catch (Exception e) {
          subscriber.onError(e); // In case there are network errors
        }
      }
    });


// Fetch from both simultaneously
    Observable<String> zipped
        = Observable.zip(fetchFromGoogle, fetchFromYahoo, new Func2<String, String, String>() {
      @Override
      public String call(String google, String yahoo) {
        // Do something with the results of both threads
        return google + "\n" + yahoo;
      }
    });

    zipped
        .subscribeOn(Schedulers.newThread()) // Create a new Thread
        .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
        .subscribe(new Action1<String>() {
          @Override
          public void call(String s) {
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

  private void button(View v) {

    Button b = (Button) v.findViewById(android.R.id.button1);// Create a Button from a layout
    Observable<Void> observable = RxView.clicks(b);// Create a ViewObservable for the Button
    Subscription buttonSub =
        observable.skip(4)// Skip the first 4 clicks
            .subscribe(new Action1<Void>() {
              @Override
              public void call(Void aVoid) {
                Timber.d("Action Clicked!");
              }
            });

    //...
    buttonSub.unsubscribe();

  }
}
