package com.vidku.andevcon_rxpatterns;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Crazy simple example of home-rolling an observable with Observable.create().
 *
 * I added the optional doOnEvent() operators to indicate all of the places where
 * you can add code before or after performing the work or handling the results.
 *
 * Created by colin on 7/26/15.
 */
public class Example3 extends Activity {

    private static final String TAG = "Example3";

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example3);

        mTextView = (TextView) findViewById(R.id.example3_textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                for (int i=0; i<=10; i++) {
                    subscriber.onNext(Integer.toString(i));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        subscriber.onError(e);
                    }
                }
                subscriber.onCompleted();
            }
        }) // all of these do() operators are optional, but can be useful
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "doOnSubscribe()");
                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d(TAG, "doOnNext() "+s);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "doOnCompleted()");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "doOnUnSubscribe()");
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "doOnError() "+ throwable.getLocalizedMessage());
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "doOnTerminate()");
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "finallyDo()");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                               @Override
                               public void call(String s) {
                                   mTextView.setText(s);
                               }
                           },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
    }
}
