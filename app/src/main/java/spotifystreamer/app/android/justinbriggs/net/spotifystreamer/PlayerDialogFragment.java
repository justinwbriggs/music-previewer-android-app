package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.service.SongService;

//TODO: on configuration change, the play/pause state is incorrect if paused

public class PlayerDialogFragment extends DialogFragment {

    private static final int PREVIEW_DURATION = 30000;

    boolean mHasRun;
    // User is interacting with seekBar
    boolean mIsSeeking;
    BroadcastReceiver mReceiver;

    private TextView mTxtArtist;
    private TextView mTxtAlbum;
    private SeekBar mSeekBar;

    private ImageButton mIbPrevious;
    private ImageButton mIbPausePlay;
    private ImageButton mIbNext;
    private ImageView mIvAlbum;
    private TextView mTxtTrack;

    private RetainedFragment mRetainedFragment;

    @Override
    public void onAttach(Activity activity) {

        // Get a reference to the retained fragment.
        FragmentManager fm = getActivity().getSupportFragmentManager();
        mRetainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // TODO: Necessary?
        setRetainInstance(true);

        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);

        mSeekBar = (SeekBar)rootView.findViewById(R.id.seek_bar);
        mIbPrevious = (ImageButton)rootView.findViewById(R.id.ib_previous);
        mIbPausePlay = (ImageButton)rootView.findViewById(R.id.ib_pause_play);
        mIbNext = (ImageButton)rootView.findViewById(R.id.ib_next);
        mTxtArtist = (TextView)rootView.findViewById(R.id.txt_artist);
        mTxtAlbum = (TextView)rootView.findViewById(R.id.txt_album);
        mIvAlbum = (ImageView)rootView.findViewById(R.id.iv_album);
        mTxtTrack = (TextView)rootView.findViewById(R.id.txt_track);

        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMediaControlAction(SongService.ACTION_PLAY_PAUSE);
            }
        });

        mIbPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMediaControlAction(SongService.ACTION_PREVIOUS);
            }
        });

        mIbNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMediaControlAction(SongService.ACTION_NEXT);
            }
        });

        return rootView;
    }

    // Communicates clicks with the SongService.
    private void sendMediaControlAction(String action) {
        Intent intent = new Intent(getActivity(), SongService.class);
        intent.setAction(action);
        getActivity().startService(intent);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // We only want to initialize one time on startup. Prevents orientation change from firing.
        if(!mHasRun) {
            disableButtons();
            // Initialize service when dialog is created, and send the trackUrl list in a bundle.
            Intent intent = new Intent(getActivity(), SongService.class);
            intent.setAction(SongService.ACTION_INITIALIZE_SERVICE);
            intent.putStringArrayListExtra(SongService.TRACK_LIST_KEY, mRetainedFragment.getTrackUrls());
            intent.putExtra(SongService.POSITION_KEY, mRetainedFragment.getPosition());
            getActivity().startService(intent);
            mHasRun = true;
        }
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

        Track track = mRetainedFragment.getTracks().get(mRetainedFragment.getPosition());

        // Set the seekBar
        // All previews are 30 seconds, but the api returns the total track length for some reason.
        mSeekBar.setMax(PREVIEW_DURATION);

        // This is for updating the current track when the user drags the seekBar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsSeeking = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                // Update the progress of the current track.
                int progress = mSeekBar.getProgress();
                Intent intent = new Intent(getActivity(), SongService.class);
                intent.setAction(SongService.ACTION_UPDATE_PROGRESS);
                intent.putExtra(SongService.PROGRESS_KEY, progress);
                getActivity().startService(intent);

                mIsSeeking = false;
            }
        });

        mTxtArtist.setText(track.artists.get(0).name);
        mTxtAlbum.setText(track.album.name);
        try {

            // 0 should be the largest image
            String imageUrl = track.album.images.get(0).url;
            Picasso.with(getActivity()).load(imageUrl)
                    .placeholder(R.drawable.ic_launcher)
                    .into(mIvAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTxtTrack.setText(track.name);

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

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Peculiar behaviour, exception happens on some configuration changes
        // Could be a bug: https://code.google.com/p/android/issues/detail?id=6191
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {

        // Register the BroadcastReceiver so SongService can send event notifications
        mReceiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SongService.BROADCAST_READY);
        intentFilter.addAction(SongService.BROADCAST_NOT_READY);
        intentFilter.addAction(SongService.BROADCAST_PLAY);
        intentFilter.addAction(SongService.BROADCAST_PAUSE);
        intentFilter.addAction(SongService.BROADCAST_TRACK_PROGRESS);
        intentFilter.addAction(SongService.BROADCAST_TRACK_CHANGED);
        intentFilter.addAction(SongService.ACTION_UPDATE_PROGRESS);

        // Use LocalBroadcastManager unless you plan on receiving broadcasts from other apps.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);
    }

    // TODO: You should be able to register this in the manifest.
    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(SongService.BROADCAST_READY)) {
                enableButtons();
            } else if(intent.getAction().equals(SongService.BROADCAST_NOT_READY)) {
                disableButtons();
            } else if(intent.getAction().equals(SongService.BROADCAST_PLAY)) {
                mIbPausePlay.setImageResource(android.R.drawable.ic_media_pause);
            } else if(intent.getAction().equals(SongService.BROADCAST_PAUSE)) {
                mIbPausePlay.setImageResource(android.R.drawable.ic_media_play);
            } else if(intent.getAction().equals(SongService.BROADCAST_TRACK_CHANGED)) {
                // When the track changes, update the RetainedFragment instance variable.
                mRetainedFragment.setPosition(intent.getIntExtra(SongService.POSITION_KEY, 0));
                updateUi();
            } else if(intent.getAction().equals(SongService.BROADCAST_TRACK_PROGRESS)) {
                // Update the seekBar in real time, only if user isn't currently touching seekBar
                if(!mIsSeeking) {
                    int progress = intent.getIntExtra(SongService.BROADCAST_TRACK_PROGRESS_KEY, 0);
                    mSeekBar.setProgress(progress);
                }
            }

        }

    }

}
