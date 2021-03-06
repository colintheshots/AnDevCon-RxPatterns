package com.colintheshots.andevcon_rxpatterns;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.annotations.Expose;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Example of forking the Retrofit chain to hide latency.
 *
 * If you create two copies of the same hot Observable, both copies
 * *should* receive the same messages. This allows you to subscribe to the
 * observable in one place to initiate an upload and then check on its
 * result much later in your application. I'm using .cache() to turn
 * Retrofit's cold observables hot.
 *
 * We've used this in my work to upload files in earlier fragments and
 * then check on the status of multiple uploads much later in the app with
 * Observable.zip() on the existing, hot Observables.
 *
 * Created by colin on 7/26/15.
 */
public class Example9 extends Activity {

    private static final String DRIVE_BASE_URL = "https://www.googleapis.com/";
    private static final String TAG = "Example9";
    private static final String FILENAME1 = "andy.png";
    private static final String MIME1 = "image/png";
    private static final String FILENAME2 = "mlogo2x_3.png";
    private static final String MIME2 = "image/png";

    private ImageView mImageView;
    private DriveClient mDriveClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example9);
        mImageView = (ImageView) findViewById(R.id.imageView);

        createDriveClient();

        File file = new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/Download/" + FILENAME1);

        File file2 = new File(
                Environment.getExternalStorageDirectory().getPath()
                        + "/Download/" + FILENAME2);

        Observable<UploadResponse> uploadResponseObservable =
                mDriveClient.mediaUpload(
                        RequestBody.create(MediaType.parse(MIME1),file),
                    MIME1)
                .cache();

        Observable<UploadResponse> uploadResponseObservable2 =
                mDriveClient.mediaUpload(
                        RequestBody.create(MediaType.parse(MIME2), file2),
                    MIME2)
                .cache();

        uploadResponseObservable
                .subscribe(new Action1<UploadResponse>() {
                @Override
                public void call(UploadResponse response) {
                    Log.d(TAG, "Uploaded file1.");
                }
            });

        uploadResponseObservable2
                .subscribe(new Action1<UploadResponse>() {
                    @Override
                    public void call(UploadResponse response) {
                        Log.d(TAG, "Uploaded file2.");
                    }
                });

        Observable.zip(uploadResponseObservable, uploadResponseObservable2,
                new Func2<UploadResponse, UploadResponse, Bitmap>() {
                    @Override
                    public Bitmap call(UploadResponse response, UploadResponse response2) {
                        Log.d(TAG, "Combining responses");
                        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                        try {
                            bitmaps.add(Picasso.with(Example9.this).load(response.thumbnailLink).get());
                            bitmaps.add(Picasso.with(Example9.this).load(response2.thumbnailLink).get());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return overlay(bitmaps.get(0),bitmaps.get(1));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap result) {
                        mImageView.setImageBitmap(result);
                }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

    }
    
    private void createDriveClient() {
        if (mDriveClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain ->
                            chain.proceed(chain.request().newBuilder().addHeader("Authorization",
                                    "Bearer " + Secrets.ACCESS_TOKEN).build())).build();

            mDriveClient = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .baseUrl(DRIVE_BASE_URL)
                    .build()
                    .create(DriveClient.class);
        }
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    private interface DriveClient {

        @Multipart
        @POST("/upload/drive/v2/files?uploadType=media")
        Observable<UploadResponse> mediaUpload(
                @Part("file") RequestBody mediaFile,
                @Header("Content-Type") String headerContentType
        );
    }

    private class UploadResponse {
        @Expose
        String thumbnailLink;
    }
}
