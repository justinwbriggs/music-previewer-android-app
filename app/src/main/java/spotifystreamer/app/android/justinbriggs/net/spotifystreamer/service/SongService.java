package spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

public class SongService extends Service {


    public static final String TRACK_URL_KEY = "track_url_key";
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_NEW_TRACK = "action_new_track";

    public static final String BROADCAST_READY = "broadcast_ready";
    public static final String BROADCAST_NOT_READY = "broadcast_not_ready";
    public static final String BROADCAST_PLAY = "broadcast_play";
    public static final String BROADCAST_PAUSE = "broadcast_pause";
    public static final String BROADCAST_COMPLETE = "broadcast_complete";

    private MediaPlayer mPlayer;

    public SongService() {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mPlayer.start();
                sendPlayerBroadcast(BROADCAST_READY);
            }
        });
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                sendPlayerBroadcast(BROADCAST_COMPLETE);
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Log.v("asdf", "action: " + intent.getAction());

        // When the host is recreated, there is no reason to take any intent commands, as the
        //

        if(intent != null) {
            // Handles any time a new track is played (oncreate, previous, next)
            if (intent.getAction().equals(ACTION_NEW_TRACK)) {
                setPlayerDataSource(intent.getStringExtra(SongService.TRACK_URL_KEY));
            }

            if (intent.getAction().equals(ACTION_PLAY_PAUSE)) {

                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    sendPlayerBroadcast(BROADCAST_PAUSE);
                } else {
                    sendPlayerBroadcast(BROADCAST_PLAY);
                    mPlayer.start();
                }


            }
        }
        return super.onStartCommand(intent, flags, startId);

    }

    public void sendPlayerBroadcast(String action) {
        Intent i = new Intent();
        i.setAction(action);
        // Use LocalBroadcastManager, it doesn't
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setPlayerDataSource(String trackUrl) {

        sendPlayerBroadcast(BROADCAST_NOT_READY);

        // Reset the player to avoid state exceptions.
        mPlayer.reset();
        try {
            mPlayer.setDataSource(trackUrl);
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