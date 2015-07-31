package com.vidku.andevcon_rxpatterns;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Debounce() example.
 *
 * This example uses debounce() for the most obvious use case.
 *
 * Search autocomplete should not send a network request every single
 * time the user presses a key. Debounce allows us to specify a quiet period
 * threshold before the search field sends each network request.
 *
 * Created by colin on 7/26/15.
 */
public class Example15 extends Activity {

    private interface GooglePlacesClient {

        @GET("/maps/api/place/autocomplete/json")
        Observable<PlacesResult> autocomplete(
                @Query("key") String key,
                @Query("input") String input);
    }

    private class PlacesResult {
        @Expose
        List<Prediction> predictions;
        @Expose
        String status;
    }

    private class Prediction {
        @Expose
        String description;
    }

    private static final String LOG_TAG = "RxRetrofitAutoComplete";
    private static final String GOOGLE_API_BASE_URL = "https://maps.googleapis.com";
    private static final int DELAY = 500;

    GooglePlacesClient mGooglePlacesClient;

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example15);
        editText = (EditText) findViewById(R.id.editText1);

        if (Secrets.API_KEY.length()<10) {
            Toast.makeText(this, "API KEY is unset!", Toast.LENGTH_LONG).show();
            return;
        }

        if (mGooglePlacesClient == null) {
            mGooglePlacesClient = new RestAdapter.Builder()
                    .setConverter(new GsonConverter(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                    .setEndpoint(GOOGLE_API_BASE_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL).build()
                    .create(GooglePlacesClient.class);
        }

        WidgetObservable.text(editText)
        .debounce(DELAY, TimeUnit.MILLISECONDS)
                .map(new Func1<OnTextChangeEvent, String>() {
                    @Override
                    public String call(OnTextChangeEvent onTextChangeEvent) {
                        return onTextChangeEvent.text().toString();
                    }
                })
                .flatMap(new Func1<String, Observable<PlacesResult>>() {
                    @Override
                    public Observable<PlacesResult> call(String s) {
                        Observable<PlacesResult> placesResult = null;
                        try {
                            placesResult = mGooglePlacesClient.autocomplete(Secrets.API_KEY, URLEncoder.encode(s, "utf8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return placesResult;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PlacesResult>() {
                    @Override
                    public void call(PlacesResult placesResult) {
                        List<String> strings = new ArrayList<String>();
                        for (Prediction p : placesResult.predictions) {
                            strings.add(p.description);
                        }
                        ListView listView = (ListView) findViewById(R.id.listView1);
                        if (listView != null) {
                            listView.setAdapter(new ArrayAdapter<String>(Example15.this, android.R.layout.simple_list_item_1, strings));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

