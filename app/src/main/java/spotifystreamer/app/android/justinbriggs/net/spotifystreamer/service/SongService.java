package spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.MainActivity;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.R;

public class SongService extends Service implements
        MediaPlayer.OnErrorListener {

    public static final String SONG_NAME_KEY = "song_name";
    public static final String TRACK_URL_KEY = "track_url";
    private boolean mIsPlaying;
    private boolean isPaused;


    MediaPlayer mPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String trackUrl = intent.getStringExtra(TRACK_URL_KEY);
        playTrack(trackUrl);

        // TODO: What is this?
        return(START_NOT_STICKY);

        //return super.onStartCommand(intent, flags, startId


    }

    public void playTrack(String trackUrl) {

        try {

            if(mPlayer == null) {
                mPlayer = new MediaPlayer();
            }

            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(trackUrl);
            mPlayer.prepare(); // might take long! (for buffering, etc)
            mPlayer.start();

            //TODO: Here we need the user to go back to the player dialog.

            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification();

            // For accessibility
            //notification.tickerText = "Playing: " + songName;
            // TODO: get a "play" icon to represent this.
            notification.icon = R.drawable.ic_launcher;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;

            notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                    "Playing a song: ", pi);

            startForeground(9376, notification);

        } catch(IOException e) {
            // TODO: Handle this
            e.printStackTrace();
        } finally {
            // TODO: Figure out what to do here.
        }

    }

    public void pauseTrack() {
        mPlayer.pause();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) mPlayer.release();
    }


}