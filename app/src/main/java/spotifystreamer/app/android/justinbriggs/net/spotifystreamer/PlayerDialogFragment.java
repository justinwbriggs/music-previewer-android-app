package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment {

    private List<Track> mTracks;
    private Track mTrack;
    private int mPosition;
    private MediaPlayer mPlayer;

    private ImageButton mIbPrevious;
    private ImageButton mIbPausePlay;
    private ImageButton mIbNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        RetainedFragment retainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());

        if(retainedFragment != null && retainedFragment.getTrack() != null) {
            mTrack = retainedFragment.getTrack();
        }
        if(retainedFragment != null && retainedFragment.getTracks() != null) {
            mTracks = retainedFragment.getTracks();
        }

        if(retainedFragment != null) {
            mPosition = retainedFragment.getPosition();
        }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);


        // Create and configure new player and set onPreparedListener
        if(mPlayer == null) {

            mPlayer = new MediaPlayer();
            try {

                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(mTrack.preview_url);
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

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    // Enable the buttons once the player is prepared.
                    mIbPausePlay.setVisibility(View.VISIBLE);

                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.v("asdf", "PlaybackComplete.");
                }
            });

        }


        mIbPausePlay = (ImageButton)rootView.findViewById(R.id.ib_pause_play);
        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // We need to check first if the player

                if(mPlayer.isPlaying()) {
                    mPlayer.pause();
                } else {
                    mPlayer.start();
                }

            }
        });

        mIbPrevious = (ImageButton)rootView.findViewById(R.id.ib_previous);
        mIbPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPosition--;
                if(mPosition == -1) {
                    mPosition = mTracks.size() - 1 ;
                }
                mTrack = mTracks.get(mPosition);
                //TODO: Update the song service with the new track.
            }
        });

        mIbNext = (ImageButton)rootView.findViewById(R.id.ib_next);
        mIbNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPosition++;
                if(mPosition == mTracks.size()) {
                    mPosition = 0;
                }
                mTrack = mTracks.get(mPosition);

                //TODO: Update the song service with the new track

            }
        });



        // Inflate the layout to use as dialog or embedded fragment
        return rootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }


}
