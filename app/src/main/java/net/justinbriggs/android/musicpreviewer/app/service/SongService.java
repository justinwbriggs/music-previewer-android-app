package net.justinbriggs.android.musicpreviewer.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.Utility;
import net.justinbriggs.android.musicpreviewer.app.activity.MainActivity;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract.TrackEntry;

import java.io.IOException;

/*
 * IntentService vs. Service for a music player
 * http://stackoverflow.com/questions/18125447/using-intentservice-for-mediaplayer-playback
 */


public class SongService extends Service {

    public static final int NOTIFICATION_ID = 1;

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
    public static final String BROADCAST_NOTIFICATION_UPDATE = "broadcast_notification_update";

    // Notify the UI that it needs to update because of track change.
    public static final String BROADCAST_TRACK_CHANGED = "broadcast_track_changed";

    Notification.Builder mNotifyBuilder;

    // Keep a reference to the album image since it is loaded asyncronously
    Bitmap mAlbumImage;

    // This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    //TODO: Is this bad practice? I'm using it to set the Share URL and determine state since I
    // don't have easy access to the service through MainActivity
    //TODO: Might need to clear these onDestroy()
    public static boolean sIsInitialized = false;
    public static String sUrl;

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

    // Loading a picasso image into a Notification is a bit tricky, so used this method:
    // http://stackoverflow.com/questions/20181491
    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAlbumImage = bitmap;
            setBuilderTrackImage();
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Only want to register a receiver the first time
        registerReceiver();

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mPlayer.start();
                sendPlayerBroadcast(BROADCAST_READY);
                // The runnable will broadcast track progress.
                mRunnable.run();
                buildNotification();
                Picasso.with(getApplicationContext()).load(
                        mCursor.getString(TrackEntry.CURSOR_KEY_ALBUM_IMAGE_URL)).into(mTarget);
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
        init();
        // If the system has to kill service due to memory, don't restart.
        return START_NOT_STICKY;
    }

    public void init() {

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

        sIsInitialized = true;
        // Get the tracks from the db
        mCursor = getApplicationContext().getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // Sort order
        );
        mCursor.moveToPosition(Utility.getCurrentTrackPositionPref(getApplicationContext()));
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
            String url = mCursor.getString(TrackEntry.CURSOR_KEY_PREVIEW_URL);
            sUrl = url;
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
        buildNotification();
    }

    public void playNextTrack() {
        // Go to the first track position if you are on the last.
        if(!mCursor.moveToNext()) {
            mCursor.moveToPosition(0);
            Utility.setCurrentTrackPositionPref(getApplicationContext(), mCursor.getPosition());
        }
        setPlayerDataSource();
    }

    public void playPreviousTrack() {
        // Go to the last track position if you are on the first.
        if(!mCursor.moveToPrevious()) {
            mCursor.moveToPosition(mCursor.getCount() - 1);
            Utility.setCurrentTrackPositionPref(getApplicationContext(), mCursor.getPosition());
        }
        setPlayerDataSource();
    }

    public void updateProgress(int progress) {
        mPlayer.seekTo(progress);
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    // We only want to build this notification one time.
    private void buildNotification() {

        //TODO: Non-critical: Should disable controls until the track has finished loading

        mNotifyBuilder = new Notification.Builder(this);

        // TODO Non-critical: An option would be to build the backstack and then display the player dialog, instead of
        // just resuming the last screen

        // The intent to open the app if the general notification is click (as opposed to a button)
        Intent broadClickIntent = new Intent(this, MainActivity.class);
        PendingIntent broadClickPendingIntent =
                PendingIntent.getActivity(this, 0, broadClickIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the common attributes first
        mNotifyBuilder
                .setSmallIcon(R.drawable.ic_notification)
                // The Large icon
                .setLargeIcon(mAlbumImage)
                .setContentIntent(broadClickPendingIntent)
                // Keeps the notification from being dismissed if broad clicked
                .setAutoCancel(false)
                .setContentTitle(mCursor.getString(TrackEntry.CURSOR_KEY_ARTIST_NAME))
                .setContentText(mCursor.getString(TrackEntry.CURSOR_KEY_TRACK_NAME));

        // Only allow the user to dismiss the notification if the player is paused.
        if(mPlayer.isPlaying()) {
            mNotifyBuilder.setOngoing(true);
        } else {
            mNotifyBuilder.setOngoing(false);
        }

        // Sets the builder options to display on lock screen or not.
        // 5.0 devices can show notifications on lock screen.
        // There are a few poorly documented caveats to displaying notifications:
        //http://stackoverflow.com/questions/26932457
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Don't display the notification time
            mNotifyBuilder.setShowWhen(false);
            // A preference determines whether or not to display on the lock screen.
            if (Utility.isShownOnLock(getApplicationContext())) {
                // Display the notifications full content on Lock Screen
                mNotifyBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                // Set the button actions
                mNotifyBuilder.setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2));
            } else {
                // SECRET means no notification at all. PRIVATE means general info with no controls.
                mNotifyBuilder.setVisibility(Notification.VISIBILITY_SECRET);
            }
        }

        //Previous intent
        Intent previousIntent = new Intent().setAction(BROADCAST_NOTIFICATION_PREVIOUS);
        PendingIntent piPrevious = PendingIntent.getBroadcast(this,
                (int)System.currentTimeMillis(), previousIntent, 0);
        mNotifyBuilder.addAction(android.R.drawable.ic_media_previous, null, piPrevious);

        // PlayPause intent
        Intent playPauseIntent = new Intent().setAction(BROADCAST_NOTIFICATION_PlAY_PAUSE);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this,
                (int)System.currentTimeMillis(), playPauseIntent, 0);
        if(mPlayer.isPlaying()) {
            mNotifyBuilder.addAction(android.R.drawable.ic_media_pause, null, piPlayPause);
        } else {
            mNotifyBuilder.addAction(android.R.drawable.ic_media_play, null, piPlayPause);
        }

        // Next Intent
        Intent nextIntent = new Intent().setAction(BROADCAST_NOTIFICATION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this,
                (int)System.currentTimeMillis(), nextIntent, 0);
        mNotifyBuilder.addAction(android.R.drawable.ic_media_next, null, pendingIntentNext);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());

    }

    public void setBuilderTrackImage() {
        mNotifyBuilder.setLargeIcon(mAlbumImage);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    private void registerReceiver() {

        mReceiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_PREVIOUS);
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_PlAY_PAUSE);
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_NEXT);
        intentFilter.addAction(SongService.BROADCAST_NOTIFICATION_UPDATE);

        // In this case, we are receiving broadcasts from Notification,
        // so don't use LocalBroadcastManager
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
            } else if(intent.getAction().equalsIgnoreCase(SongService.BROADCAST_NOTIFICATION_UPDATE)) {
                buildNotification();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Picasso.with(this).cancelRequest(mTarget);
        try {
            getApplicationContext().unregisterReceiver(mReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}