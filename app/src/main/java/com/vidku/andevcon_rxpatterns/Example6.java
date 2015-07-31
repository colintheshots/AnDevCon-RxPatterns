package com.vidku.andevcon_rxpatterns;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Example about when to unsubscribe.
 *
 * Unsubscribing is necessary when there is any chance an observable
 * may stall without reaching onCompleted() or onError() before you
 * pause or close your Activity. This avoids costly memory leaks.
 *
 * The rule of thumb is to unsubscribe in the opposite callback from
 * where you subscribe. This means you always have a subscription if one
 * is needed.
 *
 * Some observables handle their own unsubscriptions for you, like
 * ViewObservable.
 *
 * Created by colin on 7/26/15.
 */
public class Example6 extends Activity {

    private static final String TAG = "Example6";
    private Subscription createSub, startSub, resumeSub;
    private TextView mOutputTextView1, mOutputTextView2, mOutputTextView3;

    // Observable.create starts as a cold observable, so this one observable
    // will produce new function executions for each .subscribe().
    private Observable<Integer> exampleObservable =
            Observable.range(0, 99999, Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    Action1<Throwable> errorHandler = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example6);

        mOutputTextView1 = (TextView) findViewById(R.id.example6_textView1);
        mOutputTextView2 = (TextView) findViewById(R.id.example6_textView2);
        mOutputTextView3 = (TextView) findViewById(R.id.example6_textView3);

        createSub = exampleObservable
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Called unsubscribe OnDestroy()");
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer i) {
                        mOutputTextView1.setText(Integer.toString(i) + " OnCreate()");
                    }
                }, errorHandler);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startSub = exampleObservable
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Called unsubscribe OnStop()");
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer i) {
                        mOutputTextView2.setText(Integer.toString(i) + " OnStart()");
                    }
                }, errorHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();

        resumeSub = exampleObservable
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Called unsubscribe OnPause()");
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer i) {
                        mOutputTextView3.setText(Integer.toString(i) + " OnResume()");
                    }
                }, errorHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumeSub.unsubscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        startSub.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        createSub.unsubscribe();
    }
}
