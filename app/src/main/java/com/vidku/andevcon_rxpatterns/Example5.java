package com.vidku.andevcon_rxpatterns;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Example activity to demonstrate cold and hot observables.
 *
 * Created by colin on 7/26/15.
 */
public class Example5 extends Activity {

    public static final int LOOP_MAX = 100;

    TextView mColdTextView, mColdTextView2, mHotTextView, mHotTextView2;
    int mNumClicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example5);

        mColdTextView = (TextView) findViewById(R.id.coldTextView);
        mColdTextView2 = (TextView) findViewById(R.id.coldTextView2);
        mHotTextView = (TextView) findViewById(R.id.hotTextView);
        mHotTextView2 = (TextView) findViewById(R.id.hotTextView2);
        mHotTextView.setText("Click Me!");

        // Cold Observable
        //  Every time .subscribe() is called, it runs the function again.
        //  Using the .cache() operator is one of many options to make
        //  this hot.
        final Observable<String> coldObservable =
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        for (int i = 0; i <= LOOP_MAX; i++) {
                            subscriber.onNext(Integer.toString(i));
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                subscriber.onError(e);
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        coldObservable
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mColdTextView.setText("Cold: " + s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

        // pause 5 seconds, then start second subscription from cold
        // observable to show that the function executes again from scratch.

        // CAUTION: Retrofit uses COLD observables by default, which means
        // two separate subscriptions to the same observable will make two
        // network calls. Turn observables HOT if you want the same values
        // for all subscribers. Know which operators convert temperature.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                coldObservable
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                mColdTextView2.setText("Cold2: " + s);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
            }
        }, 4000);

        // Hot Observable
        //  Hot observables emit events oblivious of subscribers so
        //  that every subscriber receives the same events.
        Observable<OnClickEvent> hotObservable =
                ViewObservable.clicks(mHotTextView);

        hotObservable
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        mNumClicks++;
                        mHotTextView.setText("Hot: " + Integer.toString(mNumClicks) + " (click again)");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

        hotObservable
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        mHotTextView2.setText("Hot2: " + Integer.toString(mNumClicks));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }
}
