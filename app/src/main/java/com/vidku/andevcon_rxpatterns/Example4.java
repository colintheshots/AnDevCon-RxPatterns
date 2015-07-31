package com.vidku.andevcon_rxpatterns;

/**
 * Simple example of replacing AsyncTask. Notice what happens when
 * subscribeOn and observeOn are changed. RxJava can handle multithreading
 * and cancellation better than AsyncTask. It also lets you perform
 * work on a worker thread and handle results on the main/UI thread.
 *
 * By default, all work is done on the main thread. It's vitally
 * important to use the right scheduler for the job.
 *
 * Created by colin on 7/26/15.
 */
public class Example4 extends Example3 {
    // Duplicate of Example3.
}
