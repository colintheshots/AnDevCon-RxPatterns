package com.colintheshots.andevcon_rxpatterns;

import com.colintheshots.andevcon_rxpatterns.rxfsm.Fsm;
import com.colintheshots.andevcon_rxpatterns.rxfsm.State;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;

import rx.subjects.PublishSubject;

/**
 * Finite state machines
 *
 * RxJava handles events very, very well. So it makes sense
 * to use it to channel events to manage your state machine.
 *
 * Netflix published an early code example of this. Tobias
 * Furuholm in Sweden wrote a full library for several Rx
 * languages to manage finite state machines and their transitions.
 *
 * I ported his library to Android and may add some more features.
 * Here's a simple example of it in action.
 *
 * Created by colin on 7/26/15.
 */
public class Example18 extends Activity {

    MediaPlayer mMediaPlayer = new MediaPlayer();
    Handler mHandler = new Handler();
    TextView mStatusView;

    // these PublishSubjects are examples, but can be ANY observable where
    // any event should be used to initiate a state transition
    PublishSubject<String> mInitEvents = PublishSubject.create();
    PublishSubject<String> mReleaseEvents = PublishSubject.create();
    PublishSubject<String> mPrepareEvents = PublishSubject.create();
    PublishSubject<String> mStartEvents = PublishSubject.create();
    PublishSubject<String> mStopEvents = PublishSubject.create();
    PublishSubject<String> mPauseEvents = PublishSubject.create();

    Fsm mFsm = Fsm.create()
            .withInitialState("/idle")
            .withTopStates(
                new State("idle")
                    .withTransition("/initialized", mInitEvents, s -> {
                        try {
                            mMediaPlayer.setDataSource(this, Uri.parse(s));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .withTransition("/released", mReleaseEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.release();
                    }),
                new State("initialized")
                    .withTransition("/prepared", mPrepareEvents, s -> {
                        mStatusView.setText(s);

                        try {
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }),
                new State("prepared")
                    .withTransition("/started", mStartEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.start();
                    }, s -> {
                        try {
                            mMediaPlayer.getDuration();
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .withTransition("/stopped", mStopEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.stop();
                    }),
                new State("started")
                    .withTransition("/stopped", mStopEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.stop();
                    })
                    .withTransition("/paused", mPauseEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.pause();
                    }),
                new State("paused")
                    .withTransition("/started", mStartEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.start();
                    })
                    .withTransition("/stopped", mStopEvents, s -> {
                        mStatusView.setText(s);
                        mMediaPlayer.stop();
                    }),
                new State("stopped")
                    .withTransition("/prepared", mPrepareEvents, s -> {
                        mStatusView.setText(s);

                        try {
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example18);
        mStatusView = (TextView) findViewById(R.id.playerStatusTextView);

        mFsm.activate();

        mHandler.postDelayed(() -> {
            mStatusView.setText("Initializing...");
            mInitEvents.onNext("http://www.emp3.ws/dl/156211367/The_Rains_Of_Castamere_From_Game_Of_Thrones_Tina_Guo");
        }, 1000);

        mHandler.postDelayed(() -> mPrepareEvents.onNext("Preparing..."), 3000);

        mHandler.postDelayed(() -> mStartEvents.onNext("Starting..."), 5000);

        mHandler.postDelayed(() -> mPauseEvents.onNext("Pausing..."), 15000);

        mHandler.postDelayed(() -> mStartEvents.onNext("Restarting..."), 20000);

        mHandler.postDelayed(() -> mStopEvents.onNext("Stopping. Tyrion is da bomb!"), 45000);
    }
}
