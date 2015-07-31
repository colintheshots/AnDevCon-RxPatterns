package com.vidku.andevcon_rxpatterns;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

/**
 * Example of observing values in a BehaviorSubject.
 *
 * Observables implement the observer pattern. A BehaviorSubject
 * provides a simple implementation of an observer pattern so that all
 * subscribers are kept updated with the latest value whenever it changes
 * or a new value is requested.
 *
 * Created by colin on 7/26/15.
 */
public class Example13 extends Activity {

    private BehaviorSubject<Integer> trackUserHealth = BehaviorSubject.create();
    private int mHitpoints = 100;
    private TextView mPlayerHPTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example13);
        mPlayerHPTextView = (TextView) findViewById(R.id.playerHPTextView);

        Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mHitpoints-=10;
                        trackUserHealth.onNext(mHitpoints);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // every time we subscribe we get a new event which is a copy of the
        // most recent OnNext() call
        trackUserHealth
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer > 0) {
                            mPlayerHPTextView.setText("Your hp is " + Integer.toString(integer));
                        } else {
                            mPlayerHPTextView.setText("You're dead!");
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerHPTextView.setText("");
    }
}
