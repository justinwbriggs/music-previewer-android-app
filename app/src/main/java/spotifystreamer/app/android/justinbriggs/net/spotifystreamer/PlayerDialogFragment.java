package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service.IPlayerControls;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service.SongService;

public class PlayerDialogFragment extends DialogFragment implements ServiceConnection {

    private IPlayerControls mService = null;

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
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        mService = (IPlayerControls)iBinder;


        enableButtons();

    }



    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        disconnect();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("asdf", "onCreate()");

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


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent(getActivity(),
                SongService.class);
        intent.putExtra(SongService.TRACK_URL_KEY, mTrack.preview_url);
        intent.putExtra(SongService.TRACK_NAME_KEY, mTrack.name);

        getActivity().getApplicationContext()
                .bindService(intent, this,
                        Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().unbindService(this);
        disconnect();
        super.onDestroy();
    }

    private void disconnect() {
        mService = null;
        disableButtons();
    }


    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // TODO: Figure out why this is necessary.
        setRetainInstance(true);

        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);

        mIbPrevious = (ImageButton)rootView.findViewById(R.id.ib_previous);
        mIbPausePlay = (ImageButton)rootView.findViewById(R.id.ib_pause_play);
        mIbNext = (ImageButton)rootView.findViewById(R.id.ib_next);
        mTxtArtist = (TextView)rootView.findViewById(R.id.txt_artist);
        mTxtAlbum = (TextView)rootView.findViewById(R.id.txt_album);
        mIvAlbum = (ImageView)rootView.findViewById(R.id.iv_album);
        mTxtTrack = (TextView)rootView.findViewById(R.id.txt_track);

        // Create and configure new player and set onPreparedListener
//        if(mPlayer == null) {
//
//            mPlayer = new MediaPlayer();
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            setPlayerDataSource();
//
//            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    // Enable the buttons once the player is prepared.
//                    enableButtons();
//                }
//            });
//
//        }

        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mService.play();

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

                mService.navigate(mTrack.preview_url);
                //disableButtons();
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

                //disableButtons();
                mService.navigate(mTrack.preview_url);
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

            // 0 should be the largest image
            String imageUrl = mTrack.album.images.get(0).url;
            Picasso.with(getActivity()).load(imageUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .into(mIvAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTxtTrack.setText(mTrack.name);

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

    private void releasePlayer() {

        if(mPlayer != null) {
            if(mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

}
