package com.youtubeandroidplayerapidemo.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.youtubeandroidplayerapidemo.R;
import com.youtubeandroidplayerapidemo.utils.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sonu on 10/11/17.
 * <p>
 * ### Here you need to extend the activity with YouTubeBaseActivity otherwise it will crash the app  ###
 */

public class YoutubePlayerActivity extends YouTubeBaseActivity {
    private static final String TAG = YoutubePlayerActivity.class.getSimpleName();
    private String videoID;
    private String videoName;
    private YouTubePlayerView youTubePlayerView;
    private Tracker mTracker;
    MyPlayerStateChangeListener myPlayerStateChangeListener;
    MyPlaybackEventListener myPlaybackEventListener;
    YouTubePlayer myouTubePlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player_activity);
        //get the video id
        videoID = getIntent().getStringExtra("video_id");
        //get the video name
        videoName = getIntent().getStringExtra("video_name");
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        initializeYoutubePlayer();

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName("Title-" + videoName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        myPlaybackEventListener=new MyPlaybackEventListener();
        myPlayerStateChangeListener=new MyPlayerStateChangeListener();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    /**
     * initialize the youtube player
     */
    private void initializeYoutubePlayer() {
        youTubePlayerView.initialize(Constants.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
                                                boolean wasRestored) {

                //if initialization success then load the video id to youtube player
                if (!wasRestored) {
                    //set the player style here: like CHROMELESS, MINIMAL, DEFAULT
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

                    //load the video
                    youTubePlayer.loadVideo(videoID);
                    myouTubePlayer=youTubePlayer;
                    youTubePlayer.setPlaybackEventListener(myPlaybackEventListener);
                    youTubePlayer.setPlayerStateChangeListener(myPlayerStateChangeListener);

                    //OR

                    //cue the video
                    //youTubePlayer.cueVideo(videoID);

                    //if you want when activity start it should be in full screen uncomment below comment
                    //  youTubePlayer.setFullscreen(true);

                    //If you want the video should play automatically then uncomment below comment
                    //  youTubePlayer.play();

                    //If you want to control the full screen event you can uncomment the below code
                    //Tell the player you want to control the fullscreen change
                   /*player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    //Tell the player how to control the change
                    player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                        @Override
                        public void onFullscreen(boolean arg0) {
                            // do full screen stuff here, or don't.
                            Log.e(TAG,"Full screen mode");
                        }
                    });*/

                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
                //print or show error if initialization failed
                Log.e(TAG, "Youtube Player View initialization failed");
            }
        });
    }


    final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        private void updateLog(String prompt) {

        }

        ;

        @Override
        public void onAdStarted() {
            updateLog("onAdStarted()");
        }

        @Override
        public void onError(
                com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {
            updateLog("onError(): " + arg0.toString());
        }

        @Override
        public void onLoaded(String arg0) {
            updateLog("onLoaded(): " + arg0);
        }

        @Override
        public void onLoading() {
            updateLog("onLoading()");
        }

        @Override
        public void onVideoEnded() {
            updateLog("onVideoEnded()");
        }

        @Override
        public void onVideoStarted() {
            updateLog("onVideoStarted()");
            Toast.makeText(YoutubePlayerActivity.this, "Videos Started", Toast.LENGTH_SHORT).show();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Title-"+videoName)
                    .setAction("Start")
                    .setLabel("")
                    .build());
        }

    }

    final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        private void updateLog(String prompt) {

        }

        ;

        @Override
        public void onBuffering(boolean arg0) {
            updateLog("onBuffering(): " + String.valueOf(arg0));
        }

        @Override
        public void onPaused() {
            updateLog("onPaused()");
            long millis = myouTubePlayer.getCurrentTimeMillis();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            Toast.makeText(YoutubePlayerActivity.this, "Videos Paused", Toast.LENGTH_SHORT).show();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setAction("play duration-"+Long.toString(seconds))
                    .setLabel("Title-"+videoName)
                    .setValue(seconds)
                    .build());
        }

        @Override
        public void onPlaying() {
            updateLog("onPlaying()");
            Toast.makeText(YoutubePlayerActivity.this, "Videos Playing", Toast.LENGTH_SHORT).show();
            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    int millis = myouTubePlayer.getCurrentTimeMillis();
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

                    mTracker.send(new HitBuilders.EventBuilder()
                            .setAction(Long.toString(seconds))
                            .setLabel(videoName)
                            .setValue(5)
                            .build());
                }
            }, 0, 5, TimeUnit.SECONDS);
        }

        @Override
        public void onSeekTo(int arg0) {
            updateLog("onSeekTo(): " + String.valueOf(arg0));
        }

        @Override
        public void onStopped() {
            updateLog("onStopped()");
            long millis = myouTubePlayer.getCurrentTimeMillis();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            Toast.makeText(YoutubePlayerActivity.this, "Videos Stopped", Toast.LENGTH_SHORT).show();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setAction("play duration-"+Long.toString(seconds))
                    .setLabel("Title-"+videoName)
                    .setValue(seconds)
                    .build());
        }

    }
}
