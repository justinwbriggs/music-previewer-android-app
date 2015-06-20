package net.justinbriggs.android.musicpreviewer.app.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

public class SongService extends Service {

    public static final String ACTION_INITIALIZE_SERVICE = "initialize_service";

    public static final String TRACK_LIST_KEY = "track_list_key";
    // Refers to the position of the track list
    public static final String POSITION_KEY = "position_key";
    // Refers to the progress of the current track
    public static final String PROGRESS_KEY = "progress_key";


    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_prevous";
    public static final String ACTION_UPDATE_PROGRESS = "action_update_progress";


    // Intent key for notifying of the current tracks progress.
    public static final String BROADCAST_TRACK_PROGRESS_KEY = "broadcast_track_progress_key";
    public static final String BROADCAST_TRACK_PROGRESS = "broadcast_track_progress";

    public static final String BROADCAST_READY = "broadcast_ready";
    public static final String BROADCAST_NOT_READY = "broadcast_not_ready";
    public static final String BROADCAST_PLAY = "broadcast_play";
    public static final String BROADCAST_PAUSE = "broadcast_pause";
    // Notify the UI that it needs to update.
    public static final String BROADCAST_TRACK_CHANGED = "broadcast_track_changed";

    private int mPosition = 0;

    private MediaPlayer mPlayer;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private ArrayList<String> mTrackUrls;

    public SongService() {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mPlayer.start();
                sendPlayerBroadcast(BROADCAST_READY);
                // The runnable will broadcast track progress.
                mRunnable.run();
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playNextTrack();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Send a broadcast every second to notify the UI to update seekbar.
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(mPlayer != null){
                    int currentPosition = mPlayer.getCurrentPosition();
                    sendProgressBroadcast(currentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        };

        if(intent != null) {

            // Load up the trackUrls when the service is started.
            if (intent.getAction().equals(ACTION_INITIALIZE_SERVICE)) {

                mTrackUrls = intent.getStringArrayListExtra(TRACK_LIST_KEY);
                mPosition = intent.getIntExtra(POSITION_KEY,0);
                setPlayerDataSource();
            } else if (intent.getAction().equals(ACTION_PLAY_PAUSE)) {
               playPause();
            } else if (intent.getAction().equals(ACTION_PREVIOUS)) {
                playPreviousTrack();
            } else if (intent.getAction().equals(ACTION_NEXT)) {
                playNextTrack();
            } else if(intent.getAction().equals(ACTION_UPDATE_PROGRESS)) {
                // Jump to the requested duration.
                int progress = intent.getIntExtra(PROGRESS_KEY,0);
                updateProgress(progress);
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Send broadcasts related to player state and track list state.
    public void sendPlayerBroadcast(String action) {
        Intent i = new Intent();
        i.setAction(action);
        i.putExtra(POSITION_KEY, mPosition);
        // Use LocalBroadcastManager, more secure when you don't have to share info across apps.
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    // Send broadcasts related to progress of current track.
    public void sendProgressBroadcast(int progress) {
        Intent i = new Intent();
        i.setAction(BROADCAST_TRACK_PROGRESS);
        i.putExtra(BROADCAST_TRACK_PROGRESS_KEY, progress);
        // Use LocalBroadcastManager, more secure when you don't have to share info.
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setPlayerDataSource() {

        sendPlayerBroadcast(BROADCAST_NOT_READY);

        // Reset the player to avoid state exceptions.
        mPlayer.reset();
        try {
            String url = mTrackUrls.get(mPosition);
            mPlayer.setDataSource(url);
            //TODO: Confirm that this keeps media off main thread.
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: File may not exist. Handle this.
        } catch (IllegalArgumentException e) {
            //TODO: File may not exist. Handle this.
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        // Notify the UI that the track has changed.
        sendPlayerBroadcast(BROADCAST_TRACK_CHANGED);
    }

    public void playPause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            sendPlayerBroadcast(BROADCAST_PAUSE);
        } else {
            sendPlayerBroadcast(BROADCAST_PLAY);
            mPlayer.start();
        }
    }

    public void playNextTrack() {
        // Go to the first track position if you are on the last.
        mPosition++;
        if(mPosition == mTrackUrls.size()) {
            mPosition = 0;
        }
        setPlayerDataSource();
    }

    public void playPreviousTrack() {
        // Go to the last track position if you are on the first.
        mPosition--;
        if(mPosition == -1) {
            mPosition = mTrackUrls.size() - 1 ;
        }
        setPlayerDataSource();
    }

    public void updateProgress(int progress) {
        mPlayer.seekTo(progress);
    }



//            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//                    new Intent(getApplicationContext(), MainActivity.class),
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//            Notification notification = new Notification();
//
//            // For accessibility
//            //notification.tickerText = "Playing: " + songName;
//            // TODO: get a "play" icon to represent this.
//            notification.icon = R.drawable.ic_launcher;
//            notification.flags |= Notification.FLAG_ONGOING_EVENT;
//
//            notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
//                    "Playing a song: ", pi);
//
//            startForeground(9376, notification);


}