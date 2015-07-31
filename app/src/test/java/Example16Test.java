import org.junit.Test;

import java.lang.Long;
import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
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
    public void using_testscheduler_to_simulate_network_events() {
        // TestScheduler lets you advance time by hand
        TestScheduler scheduler = new TestScheduler();

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
            }
        }, 1000, TimeUnit.MILLISECONDS);

        scheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS);

        networkSubject.subscribeOn(scheduler)
                .subscribe(new Action1<NetworkResponse>() {
                    @Override
                    public void call(NetworkResponse networkResponse) {
                        System.out.println(networkResponse.httpCode);
                        assertEquals(networkResponse.httpCode, 200);
                    }
                });
    }

    private class NetworkResponse {
        int httpCode;
    }
}
