package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment {

    private List<Track> mTracks;
    private Track mTrack;
    private int mPosition;
    private MediaPlayer mPlayer;

    private TextView mTxtArtist;
    private TextView mTxtAlbum;

    private ImageButton mIbPrevious;
    private ImageButton mIbPausePlay;
    private ImageButton mIbNext;
    private ImageView mIvAlbum;
    private TextView mTxtTrack;

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

        mIbPrevious = (ImageButton)rootView.findViewById(R.id.ib_previous);
        mIbPausePlay = (ImageButton)rootView.findViewById(R.id.ib_pause_play);
        mIbNext = (ImageButton)rootView.findViewById(R.id.ib_next);
        mTxtArtist = (TextView)rootView.findViewById(R.id.txt_artist);
        mTxtAlbum = (TextView)rootView.findViewById(R.id.txt_album);
        mIvAlbum = (ImageView)rootView.findViewById(R.id.iv_album);
        mTxtTrack = (TextView)rootView.findViewById(R.id.txt_track);

        // Create and configure new player and set onPreparedListener
        if(mPlayer == null) {

            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            setPlayerDataSource();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // Enable the buttons once the player is prepared.
                    enableButtons();
                }
            });

        }

        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPlayer.isPlaying()) {
                    mPlayer.pause();
                } else {
                    mPlayer.start();
                }

            }
        });

        mIbPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to the last track position if you are on the first.
                mPosition--;
                if(mPosition == -1) {
                    mPosition = mTracks.size() - 1 ;
                }
                mTrack = mTracks.get(mPosition);

                // We'll need to disable the buttons again, which onPrepared will handle re-enabling
                disableButtons();
                setPlayerDataSource();
                updateUi();

            }
        });

        mIbNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to the first track position if you are on the last.
                mPosition++;
                if(mPosition == mTracks.size()) {
                    mPosition = 0;
                }
                mTrack = mTracks.get(mPosition);
                disableButtons();
                setPlayerDataSource();
                updateUi();

            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disableButtons();
        updateUi();
    }

    private void disableButtons() {

        mIbPrevious.setEnabled(false);
        mIbPausePlay.setEnabled(false);
        mIbNext.setEnabled(false);

        // Android doesn't provide "disabled" version of their media buttons, so
        // apply some alpha to the image to simulate disabled.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mIbPrevious.setImageAlpha(50);
            mIbPausePlay.setImageAlpha(50);
            mIbNext.setImageAlpha(50);
        }

    }

    private void enableButtons() {

        mIbPrevious.setEnabled(true);
        mIbPausePlay.setEnabled(true);
        mIbNext.setEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mIbPrevious.setImageAlpha(255);
            mIbPausePlay.setImageAlpha(255);
            mIbNext.setImageAlpha(255);
        }

    }

    private void updateUi() {

        mTxtArtist.setText(mTrack.artists.get(0).name);
        mTxtAlbum.setText(mTrack.album.name);
        try {

            // TODO: Verify this returns the appropriate image size.
            String imageUrl = mTrack.album.images.get(0).url;
            // Always get the last image, which should be the 64 px size, but may not be included.
            Picasso.with(getActivity()).load(imageUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .into(mIvAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTxtTrack.setText(mTrack.name);

    }


    private void setPlayerDataSource() {

        // Reset the player to avoid state exceptions.
        mPlayer.reset();

        try {
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
