package com.vidku.andevcon_rxpatterns;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Example of an RxJava-based event bus.
 *
 * At its core, RxJava and reactive programming is designed as a means
 * of handling a stream of events. This is the same goal accomplished by
 * event buses. An RxJava PublishSubject makes an ideal event bus, so there
 * is no good reason to include an event bus library in an app utilizing
 * RxJava.
 *
 * Example borrowed from Ben Christenson.
 *
 * Created by colin on 7/26/15.
 */
public class Example12 extends Activity {

    private static final String TAG = "Example12";

    MyEventBus bus = new MyEventBus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bus.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        // You can check the type here and cast as needed
                        Log.d(TAG, o.toString());
                    }
                });

        bus.send(12);
    }

    public class MyEventBus {
        private final PublishSubject<Object> bus = PublishSubject.create();
        /**
         * If multiple threads are going to emit events to this then it must be made thread-safe like this instead:
         */
        //        private final Subject<Object, Object> bus = new SerializedSubject<Object, Object>(PublishSubject.create());

        public void send(Object o) {
            bus.onNext(o);
        }

        public Observable<Object> toObservable() {
            return bus;
        }
    }

}
