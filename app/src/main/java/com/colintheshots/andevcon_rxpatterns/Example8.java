package com.colintheshots.andevcon_rxpatterns;

import android.app.Activity;
import android.os.Bundle;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * flatMap() to chain Retrofit requests example.
 *
 * I'm using flatMap() to chain three requests.
 *
 * The first request grabs the user's list of GitHub gists.
 *
 * The second request grab that user's latest gist and grabs the contents
 * of a file from that gist.
 *
 * The third request send that file string to Twilio to initiate a phone
 * message.
 *
 * Created by colin on 7/26/15.
 */
public class Example8 extends Activity {


    /** The GitHub REST API Endpoint */
    public final static String GITHUB_BASE_URL = "https://api.github.com/";

    /** The Twilio REST API Endpoint */
    private final static String TWILIO_BASE_URL = "https://api.twilio.com/2010-04-01/";

    private GitHubClient mGitHubClient;
    private TwilioInterface mTwilioInterface;
    private static HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createGithubClient();
        createTwilioClient();

        mGitHubClient.gists()
                .flatMap(new Func1<List<Gist>, Observable<GistDetail>>() {
                    @Override
                    public Observable<GistDetail> call(List<Gist> gists) {
                        return mGitHubClient.gist(gists.get(0).getId());
                    }
                })
                .flatMap(new Func1<GistDetail, Observable<CallResponse>>() {

                    @Override
                    public Observable<CallResponse> call(GistDetail gistDetail) {
                        try {
                            List<String> keysAsArray = new ArrayList<String>(gistDetail.getFiles().keySet());
                            String content = gistDetail.getFiles().get(keysAsArray.get(0)).getContent();
                            return mTwilioInterface.makeCall(
                                    Secrets.twilio_accountsid,
                                    Secrets.twilio_from_number,
                                    Secrets.twilio_to_number,
                                    "http://twimlets.com/menu?Message="
                                            + URLEncoder.encode(content, "UTF-8")
                            );
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CallResponse>() {
                    @Override
                    public void call(CallResponse callResponse) {
                        // Do nothing really
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void createGithubClient() {
        if (mGitHubClient == null) {
            mGitHubClient = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .client(new OkHttpClient.Builder().addInterceptor(mLoggingInterceptor)
                            .addInterceptor(chain ->
                                    chain.proceed(chain.request().newBuilder().addHeader("Authorization",
                                            "token " + Secrets.GITHUB_PERSONAL_ACCESS_TOKEN).build())).build())
                    .baseUrl(GITHUB_BASE_URL)
                    .build()
                    .create(GitHubClient.class);
        }
    }

    private void createTwilioClient() {
        if (mTwilioInterface == null) {
            mTwilioInterface = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .client(new OkHttpClient.Builder().addInterceptor(mLoggingInterceptor)
                            .addInterceptor(chain ->
                                    chain.proceed(chain.request().newBuilder().addHeader("Authorization",
                                            Credentials.basic(Secrets.twilio_accountsid, Secrets.twilio_authtoken))
                                            .build())).build())
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                    .baseUrl(TWILIO_BASE_URL)
                    .build()
                    .create(TwilioInterface.class);
        }
    }

    private interface TwilioInterface {
        @FormUrlEncoded
        @POST("Accounts/{accountsid}/Calls.json")
        Observable<CallResponse> makeCall(
                @Path("accountsid") String accountsid,
                @Field("From") String from,
                @Field("To") String to,
                @Field("Url") String url
        );
    }

    public interface GitHubClient {
        @GET("/gists")
        Observable<List<Gist>> gists();

        @GET("/gists/{id}")
        Observable<GistDetail> gist(@Path("id") String id);
    }

    private class CallResponse {
        public String sid;
    }

    private class Gist {
        @Expose
        private String id;

        @Expose
        private String description;

        @Expose
        private String html_url;

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getHtml_url() {
            return html_url;
        }
    }

    private class GistDetail {
        @Expose
        private Map<String, GistFile> files;

        public Map<String, GistFile> getFiles() {
            return files;
        }
    }

    private class GistFile {
        @Expose
        private String content;

        public String getContent() {
            return content;
        }
    }
}
