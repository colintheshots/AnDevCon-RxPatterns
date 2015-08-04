import junit.framework.Assert;

import org.junit.Test;

import java.lang.Long;
import java.lang.Override;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Example of using test schedulers to inject events anywhere you have an
 * observable.
 *
 * Many Observable operators accept a scheduler parameter. Use it.
 *
 * Created by colin on 7/31/15.
 */
public class Example16Test {

    // http://stackoverflow.com/questions/26699147/how-to-use-testscheduler-in-rxjava
    @Test
    public void should_test_observable_interval() {
        TestScheduler scheduler = new TestScheduler();
        final List<Long> result = new ArrayList<>();
        Observable.interval(1, TimeUnit.SECONDS, scheduler)
                .take(5)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        result.add(aLong);
                    }
                });
        assertTrue(result.isEmpty());
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        assertEquals(2, result.size());
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        assertEquals(5, result.size());
    }

    @Test
    public void test_with_blocking_observable() {
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> ints = Observable.range(1, 5)
                .take(5)
                .toList()
                .toBlocking()
                .single();
        Assert.assertEquals(expected, ints);
    }

    @Test
    public void using_testscheduler_to_simulate_network_events() {

        // TestScheduler lets you advance time by hand
        TestScheduler scheduler = Schedulers.test();
        TestSubscriber<NetworkResponse> subscriber = new TestSubscriber<>();

        // Scheduler.Worker lets you schedule events in time
        Scheduler.Worker worker = scheduler.createWorker();

        // Subjects allow both input and output, so they can be swapped in for
        // Retrofit calls to unit test your code.
        final PublishSubject<NetworkResponse> networkSubject = PublishSubject.create();
        worker.schedule(new Action0() {
            @Override
            public void call() {
                NetworkResponse networkResponse = new NetworkResponse();
                networkResponse.httpCode = 401;
                networkSubject.onNext(networkResponse);
                networkSubject.onCompleted();
            }
        }, 1000, TimeUnit.MILLISECONDS);

        worker.schedule(new Action0() {
            @Override
            public void call() {
                networkSubject.onCompleted();
            }
        }, 2000, TimeUnit.MILLISECONDS);

        NetworkResponse expected = new NetworkResponse();
        expected.httpCode = 401;

        networkSubject
                .subscribeOn(scheduler)
                .subscribe(subscriber);

        scheduler.advanceTimeBy(1500, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Arrays.asList(expected));

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertUnsubscribed();
    }

    private class NetworkResponse {
        int httpCode;

        @Override
        public boolean equals(Object o) {
            if (o instanceof NetworkResponse) {
                return ((NetworkResponse) o).httpCode == httpCode;
            } else {
                return false;
            }
        }
    }
}
