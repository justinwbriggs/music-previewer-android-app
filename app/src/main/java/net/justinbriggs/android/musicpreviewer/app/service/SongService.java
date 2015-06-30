package net.justinbriggs.android.musicpreviewer.app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.Utils;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;

import java.io.IOException;

/*
 * IntentService vs. Service for a music player
 * http://stackoverflow.com/questions/18125447/using-intentservice-for-mediaplayer-playback
 */

public class SongService extends Service {

    public static final String ACTION_INITIALIZE_PLAYER = "initialize_player";
    public static final String POSITION_KEY = "position_key";

    // Intent key for notifying of the current tracks progress.
    public static final String BROADCAST_TRACK_PROGRESS_KEY = "broadcast_track_progress_key";
    public static final String BROADCAST_TRACK_PROGRESS = "broadcast_track_progress";

    public static final String BROADCAST_READY = "broadcast_ready";
    public static final String BROADCAST_NOT_READY = "broadcast_not_ready";
    public static final String BROADCAST_PLAY_PAUSE = "broadcast_play_pause";

    public static final String BROADCAST_NOTIFICATION_PREVIOUS = "broadcast_notification_previous";
    public static final String BROADCAST_NOTIFICATION_PlAY_PAUSE = "broadast_notification_play_pause";
    public static final String BROADCAST_NOTIFICATION_NEXT = "broadcast_notification_next";

    // Notify the UI that it needs to update because of track change.
    public static final String BROADCAST_TRACK_CHANGED = "broadcast_track_changed";

    // This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    // TODO: Not sure if this is the best method for other components to determine player state
    public static boolean sIsInitialized = false;


    // Once this is set, components can request it through getCurrentCursor(). The PlayerDialogFragment
    // will use it to update the UI when the Now Playing button is pressed, but will use a different
    // cursor from the db to maintain the UI relative to the track being played.
    Cursor mCursor;

    BroadcastReceiver mReceiver;

    private MediaPlayer mPlayer;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Cursor getCurrentCursor() {
        return mCursor;
    }

    @Override
    public void onCreate() {
        super.onCreate();


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
        // Send a broadcast every second to notify the component UI to update seekbar.
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

        // Should be able to just call init() from component, crashes with null pointer
        if(intent != null) {
            //Load up the trackUrls from the db when the service is started.
            if (intent.getAction().equals(ACTION_INITIALIZE_PLAYER)) {
                registerReceiver();
                init();
            }
        }

        // If the system has to kill service due to memory, don't restart.
        return START_NOT_STICKY;
    }

    public void init() {
        sIsInitialized = true;
        // Get the tracks from the db
        mCursor = getApplicationContext().getContentResolver().query(
                MusicContract.TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // Sort order
        );
        mCursor.moveToPosition(Utils.getCurrentTrackPositionPref(getApplicationContext()));
        setPlayerDataSource();

        initNotification();
    }

    // Send broadcasts related to player state and track list state.
    public void sendPlayerBroadcast(String action) {
        Intent i = new Intent();
        i.setAction(action);
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

    private void setPlayerDataSource() {
        sendPlayerBroadcast(BROADCAST_NOT_READY);

        // Reset the player to avoid state exceptions.
        mPlayer.reset();
        try {
            //TODO: Go back and add constants for the column keys.
            String url = mCursor.getString(5);
            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: File may not exist. Handle this.
        } catch (IllegalArgumentException e) {
            //TODO: File may not exist. Handle this.
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        // Notify components that the track has changed.
        sendPlayerBroadcast(BROADCAST_TRACK_CHANGED);

    }

    public void playPause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        // Notify components that track has been successfully started or paused.
        sendPlayerBroadcast(BROADCAST_PLAY_PAUSE);
    }

    public void playNextTrack() {
        // Go to the first track position if you are on the last.
        if(!mCursor.moveToNext()) {
            mCursor.moveToPosition(0);
            Utils.setCurrentTrackPositionPref(getApplicationContext(), mCursor.getPosition());
        }
        setPlayerDataSource();
    }

    public void playPreviousTrack() {
        // Go to the last track position if you are on the first.
        if(!mCursor.moveToPrevious()) {
            mCursor.moveToPosition(mCursor.getCount() - 1);
            Utils.setCurrentTrackPositionPref(getApplicationContext(), mCursor.getPosition());
        }
        setPlayerDataSource();
    }
    public void updateProgress(int progress) {
        mPlayer.seekTo(progress);
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    private void initNotification() {

        // Create a new PlayerNotification object.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_play)
                        .setContentTitle("TestTitle")
                        .setSubText("Subtext")
                        .setAutoCancel(false);

        //Previous intent
        Intent previousIntent = new Intent();
        previousIntent.setAction(BROADCAST_NOTIFICATION_PREVIOUS);
        PendingIntent piPrevious = PendingIntent.getBroadcast(this, 1, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_previous, null, piPrevious);

        // PlayPause intent
        Intent playPauseIntent = new Intent();
        playPauseIntent.setAction(BROADCAST_NOTIFICATION_PlAY_PAUSE);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_pause, null, piPlayPause);

        // Next Intent
        Intent nextIntent = new Intent();
        nextIntent.setAction(BROADCAST_NOTIFICATION_NEXT);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_next, null, pendingIntentNo);

        notificationManager.notify(1, mBuilder.build());
    }

    private void registerReceiver() {

        mReceiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_PREVIOUS);
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_PlAY_PAUSE);
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_NEXT);

        // Use LocalBroadcastManager unless you plan on receiving broadcasts from other apps.
        //LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);

        // In this case, we are receiving broadcasts from Notification
        getApplicationContext().registerReceiver(mReceiver, intentFilter);
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SongService.BROADCAST_NOTIFICATION_PREVIOUS)) {
                playPreviousTrack();
            } else if(intent.getAction().equals(SongService.BROADCAST_NOTIFICATION_PlAY_PAUSE)) {
                playPause();
            } else if(intent.getAction().equalsIgnoreCase(SongService.BROADCAST_NOTIFICATION_NEXT)) {
                playNextTrack();
            }
        }
    }


    public void updateNotification() {

    }

}