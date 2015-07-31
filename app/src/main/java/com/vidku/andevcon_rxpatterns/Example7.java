package com.vidku.andevcon_rxpatterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Example to illustrate the difference between flatMap() and map()
 *
 * flatMap() flattens the Observable value you return. This means if you
 * return Observable<Observable<T>>> that each event is pushed into a new
 * Observable of type <Observable<T>>. This means you can use all of your
 * standard RxJava chained methods on nested Observables.
 *
 * map() changes the type of an observable, but does not flatten it.
 * If you return Observable<Observable<T>>> you will easily be able to
 * apply standard observable operators to the result.
 *
 * Created by colin on 7/26/15.
 */
public class Example7 extends Activity {

    TextView mTextView, mTextView2;
    WeatherInterface mWeatherInterface;

    Observable<Integer> mIntegerObservable1 = Observable.range(1,100000)
            .delay(10, TimeUnit.MILLISECONDS);
    Observable<Integer> mIntegerObservable2 = Observable.range(1,100000)
            .delay(10, TimeUnit.MILLISECONDS);

    Observable<String> mObservable1 =
            Observable.just("42.346447")
                    .delay(10, TimeUnit.SECONDS)
                    .repeat(3);
    Observable<String> mObservable2 =
            Observable.just("-71.083534")
                    .delay(30, TimeUnit.SECONDS)
                    .repeat(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example7);

        mTextView = (TextView) findViewById(R.id.example7_textView);
        mTextView2 = (TextView) findViewById(R.id.example7_textView2);

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        mWeatherInterface = new RestAdapter.Builder()
                .setEndpoint("http://api.openweathermap.org")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build()
                .create(WeatherInterface.class);


        // Since we're returning a basic type from Observable.zip()
        // instead of an observable of a type, we can simply map to
        // the new type. In fact, the cast() operator is probably
        // good enough here.
        Observable.zip(mIntegerObservable1, mIntegerObservable2,
                new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2) {
                        return integer + integer2;
                    }
                })
            .map(new Func1<Integer, String>() {
                @Override
                public String call(Integer integer) {
                    return Integer.toString(integer);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    mTextView.setText(s);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

        Observable.zip(mObservable1, mObservable2,
                new Func2<String, String, Observable<CityWeather>>() {
                    @Override
                    public Observable<CityWeather> call(String lat, String lon) {
                        return mWeatherInterface.getWeather(lat, lon);
                    }
                })
        .flatMap(new Func1<Observable<CityWeather>, Observable<CityWeather>>() {
            @Override
            public Observable<CityWeather> call(Observable<CityWeather> cityWeatherObservable) {
                return cityWeatherObservable;
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<CityWeather>() {
            @Override
            public void call(CityWeather cityWeather) {
                mTextView2.setText(
                        "City: " + cityWeather.name +
                                "\nTemp: " + Float.toString(cityWeather.main.temp) +
                                " Kelvins"
                );
            }
        });

    }

    private interface WeatherInterface {

        @GET("/data/2.5/weather")
        Observable<CityWeather> getWeather(
                @Query("lat") String lat,
                @Query("lon") String lon
        );
    }


    class CityWeather {
        @Expose
        MainData main;

        @Expose
        String name;
    }

    class MainData {
        @Expose
        Float temp;
    }
}
