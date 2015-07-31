package com.vidku.andevcon_rxpatterns;

import android.app.Activity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Compose() example.
 *
 * The compose() operator allows us to provide shorthand methods for common
 * reactive programming idioms in our app.
 *
 * Example shamelessly borrowed from my friend Dan Lew back in Minneapolis.
 * http://blog.danlew.net/2015/03/02/dont-break-the-chain/
 *
 * Created by colin on 7/26/15.
 */
public class Example17 extends Activity {

    // Let's use compose to wrap some operators we use a lot!
    <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    // Now we have a shorthand for applying our schedulers in one operator
    Observable<String> testObservable =
            Observable.just("Hello world!")
            .compose(this.<String>applySchedulers());

    // this will look much cleaner without needlessly specifying type
    // if you compile using JDK 8 and retrolamba
}
