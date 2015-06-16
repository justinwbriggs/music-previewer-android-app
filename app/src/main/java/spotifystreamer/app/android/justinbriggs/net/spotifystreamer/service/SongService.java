package spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;


// A service is NOT a separate process or thread.

// Should this be an IntentService? You might be able to, looks like it handles intents


// An IntentService doesn't need to handle multiple requests simultaneously.
public class SongService extends Service {


    public static final String TRACK_URL_KEY = "track_url_key";
    public static final String TRACK_NAME_KEY = "track_name_key";

    // Need to create a new thread inside this service in order to keep it off the main thread. This
    // can probably be handled with prepareAsync();
    // Does the MediaPlayer have a way of storing multiple "tracks"?

//    If a component calls bindService() to create the service (and onStartCommand() is not called),
//    then the service runs only as long as the component is bound to it. Once the service is unbound
//    from all clients, the system destroys it.
    // Must run in foreground?

    // Bind to the service, establishing a bi-directional communications channel that lasts as long as the client needs it
    // You'll need a service connection which represents the client side of the binding




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        String trackUrl = intent.getStringExtra(TRACK_URL_KEY);
        String trackName = intent.getStringExtra(TRACK_NAME_KEY);
        return new PlayerBinder(trackUrl, trackName);

    }


    private static class PlayerBinder extends Binder
            implements IPlayerControls {


        @Override
        public void onMediaPrepared() {

        }

        private MediaPlayer mPlayer;

        public PlayerBinder(String trackUrl, String trackName) {
            Log.v("asdf", "PlayerBinder()");

            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            setPlayerDataSource(trackUrl);

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    Log.v("asfd", "onPrepared");
                    mEventListener.onPrepared();

                    // Notify the activity that the media is prepared so you can update button states.


                }
            });

        }


        private void setPlayerDataSource(String trackUrl) {

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
                e.printStackTrace();
                //TODO: File may not exist. Handle this.
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void play() {
            mPlayer.start();
        }

        @Override
        public void pause() {
            mPlayer.pause();
        }

        // For navigating to
        @Override
        public void navigate(String trackUrl) {
            setPlayerDataSource(trackUrl);
        }
    }




//    public void playTrack(String trackUrl) {
//
//        try {
//
//            if(mPlayer == null) {
//                mPlayer = new MediaPlayer();
//            }
//
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mPlayer.setDataSource(trackUrl);
//            mPlayer.prepare(); // might take long! (for buffering, etc)
//            mPlayer.start();
//
//            //TODO: Here we need the user to go back to the player dialog.
//
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
//
//        } catch(IOException e) {
//            // TODO: Handle this
//            e.printStackTrace();
//        } finally {
//            // TODO: Figure out what to do here.
//        }
//
//    }





}