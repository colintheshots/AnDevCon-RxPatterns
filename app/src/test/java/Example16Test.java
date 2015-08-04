import junit.framework.Assert;

import org.junit.Test;

import java.lang.Long;
import java.lang.Override;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
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

        // schedule a first observable event to occur at 1000 ms
        worker.schedule(new Action0() {
            @Override
            public void call() {
                // explicitly calling onNext in a worker allows one to
                // create a very specific test of timed events
                networkSubject.onNext(new NetworkResponse(401));
            }
        }, 1000, TimeUnit.MILLISECONDS);

        // schedule a second observable event to occur at 2000 ms
        worker.schedule(new Action0() {
            @Override
            public void call() {
                networkSubject.onNext(new NetworkResponse(200));
                networkSubject.onCompleted();
            }
        }, 2000, TimeUnit.MILLISECONDS);

        // we must subscribe before anticipating results
        networkSubject
                .subscribeOn(scheduler)
                .subscribe(subscriber);

        // we can manually advance time using the scheduler and check assertions
        scheduler.advanceTimeBy(1500, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Arrays.asList(
                new NetworkResponse(401)));

        // awaitTerminalEvent will wait forever if we don't advance time enough
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        subscriber.awaitTerminalEvent();
        subscriber.assertReceivedOnNext(Arrays.asList(
                new NetworkResponse(401),
                new NetworkResponse(200)));

        // TestSubscriber provides many useful methods
        subscriber.assertNoErrors();
        subscriber.assertValueCount(2);
        subscriber.assertUnsubscribed();
    }

    @Test
    public void test_anomalous_network_event() {

        // TestScheduler lets you advance time by hand
        TestScheduler scheduler = Schedulers.test();
        TestSubscriber<NetworkResponse> subscriber = new TestSubscriber<>();

        // Scheduler.Worker lets you schedule events in time
        Scheduler.Worker worker = scheduler.createWorker();

        // Subjects allow both input and output, so they can be swapped in for
        // Retrofit calls to unit test your code.
        final PublishSubject<NetworkResponse> networkSubject = PublishSubject.create();

        // schedule a first observable event to occur at 1000 ms
        worker.schedule(new Action0() {
            @Override
            public void call() {
                networkSubject.onError(new TimeoutException());
            }
        }, 10000, TimeUnit.MILLISECONDS);

        // subscribing so events appear
        networkSubject
                .subscribeOn(scheduler)
                .subscribe(subscriber);

        scheduler.advanceTimeBy(20000, TimeUnit.MILLISECONDS);

        subscriber.awaitTerminalEvent();

        // we use the class-based assertError method, since it's easier to match
        subscriber.assertError(TimeoutException.class);
        subscriber.assertValueCount(0);
        subscriber.assertUnsubscribed();
    }

    private class NetworkResponse {
        int httpCode;

        NetworkResponse(int code) {
            httpCode = code;
        }

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
