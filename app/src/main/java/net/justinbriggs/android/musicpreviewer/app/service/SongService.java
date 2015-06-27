package net.justinbriggs.android.musicpreviewer.app.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import net.justinbriggs.android.musicpreviewer.app.Utils;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;

import java.io.IOException;

/*
 * IntentService vs. Service for a music player
 * http://stackoverflow.com/questions/18125447/using-intentservice-for-mediaplayer-playback
 */

public class SongService extends Service {

    // This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    // TODO: Not sure if this is the best method for other components to determine player state
    public static boolean sIsInitialized = false;

    public static final String ACTION_INITIALIZE_PLAYER = "initialize_player";
    public static final String POSITION_KEY = "position_key";

    // Intent key for notifying of the current tracks progress.
    public static final String BROADCAST_TRACK_PROGRESS_KEY = "broadcast_track_progress_key";
    public static final String BROADCAST_TRACK_PROGRESS = "broadcast_track_progress";

    public static final String BROADCAST_READY = "broadcast_ready";
    public static final String BROADCAST_NOT_READY = "broadcast_not_ready";
    public static final String BROADCAST_PLAY_PAUSE = "broadcast_play_pause";

    // Notify the UI that it needs to update because of track change.
    public static final String BROADCAST_TRACK_CHANGED = "broadcast_track_changed";

    // Once this is set, components can request it through getCurrentCursor(). The PlayerDialogFragment
    // will use it to update the UI when the Now Playing button is pressed, but will use a different
    // cursor from the db to maintain the UI relative to the track being played.
    Cursor mCursor;

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
               init();
            }
        }
        return super.onStartCommand(intent, flags, startId);
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

}