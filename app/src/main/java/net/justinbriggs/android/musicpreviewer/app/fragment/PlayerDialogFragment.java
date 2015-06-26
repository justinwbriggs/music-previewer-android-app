package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;

public class PlayerDialogFragment extends DialogFragment {


    public static final String FRAGMENT_TAG = PlayerDialogFragment.class.getSimpleName();
    private static final int PREVIEW_DURATION = 30000;

    boolean mHasRun;
    boolean mFromActionBar;
    int mPosition;

    // I had to create this class variable because getActivity.getContentResolver() returns null
    // when navigating back from the dialog to this fragment, then clicking another track. Not sure why.
    private ContentResolver mContentResolver;

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

    public static PlayerDialogFragment newInstance(int position) {

        //TODO: We'll be passing the position off to the SongService for now, but keeping it in sync
        // is going to be an issue. Might want to see if you can just allow for this dialog to
        // get the current position from the SongService.
        PlayerDialogFragment f = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SongService.POSITION_KEY, position);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContentResolver = getActivity().getContentResolver();

        // Tells the host activity that the fragment has menu options it wants to manipulate.
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // The position can either be defined from the bundle sent from TrackListFragment, or
        // from being saved on rotation.
        if(savedInstanceState != null && savedInstanceState.containsKey(SongService.POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(SongService.POSITION_KEY);
        } else if(getArguments() != null && getArguments().containsKey(SongService.POSITION_KEY)) {
            mPosition = getArguments().getInt(SongService.POSITION_KEY);
        } else {
            // This implies that the DialogFragment is being created from the Now Playing button.
            mFromActionBar = true;
            // Get mPosition from the running service in order to update the ui correctly.
            sendMediaControlAction(SongService.ACTION_GET_POSITION);

        }


        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);

        mSeekBar = (SeekBar)rootView.findViewById(R.id.seek_bar);
        mIbPrevious = (ImageButton)rootView.findViewById(R.id.ib_previous);
        mIbPausePlay = (ImageButton)rootView.findViewById(R.id.ib_pause_play);
        mIbNext = (ImageButton)rootView.findViewById(R.id.ib_next);
        mTxtArtist = (TextView)rootView.findViewById(R.id.txt_artist);
        mTxtAlbum = (TextView)rootView.findViewById(R.id.txt_album);
        mIvAlbum = (ImageView)rootView.findViewById(R.id.iv_album);
        mTxtTrack = (TextView)rootView.findViewById(R.id.txt_track);

        // Used to configure the play/pause button on rotation.
        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("asdf", "PlayClicked");
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

            // We also don't want to re-initialize if the user resumes from action bar button.
            if(!mFromActionBar) {
                disableButtons();
                // Initialize service when dialog is created, and send the trackUrl list in a bundle.
                Intent intent = new Intent(getActivity(), SongService.class);
                intent.setAction(SongService.ACTION_INITIALIZE_SERVICE);
                intent.putExtra(SongService.POSITION_KEY, mPosition);
                getActivity().startService(intent);
                mHasRun = true;
            }
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

        //TODO: Might make this an instance variable.
        Cursor cursor = mContentResolver.query(
                MusicContract.TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // Sort order
        );
        cursor.moveToPosition(mPosition);

        // Set the seekBar
        // All previews are 30 seconds, but the api returns the total track length for some reason.
        mSeekBar.setMax(PREVIEW_DURATION);

        // This is for updating the current track when the user drags the seekBar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

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

        mTxtArtist.setText(cursor.getString(2));
        mTxtAlbum.setText(cursor.getString(1));
        try {

            // 0 should be the largest image
            //TODO: Make sure you record the thumbnail and the large image in the db
            String imageUrl = cursor.getString(4);
            Picasso.with(getActivity()).load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(mIvAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTxtTrack.setText(cursor.getString(3));
        cursor.close();

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
            //e.printStackTrace();
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
        intentFilter.addAction(SongService.BROADCAST_POSITION);

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
                // TODO: We'll need to sync up the location between this fragment and the service.
                mPosition = intent.getIntExtra(SongService.POSITION_KEY, 0);
                updateUi();
            } else if(intent.getAction().equals(SongService.BROADCAST_TRACK_PROGRESS)) {
                // Update the seekBar in real time, only if user isn't currently touching seekBar
                if(!mIsSeeking) {
                    int progress = intent.getIntExtra(SongService.BROADCAST_TRACK_PROGRESS_KEY, 0);
                    mSeekBar.setProgress(progress);
                }
            } else if (intent.getAction().equals(SongService.BROADCAST_POSITION)) {
                mPosition = intent.getIntExtra(SongService.POSITION_KEY, 0);
                updateUi();
            }
        }
    }

    @Override
    public void onDestroyView() {
        // Used to fix the disappearing dialog after rotation on tablets
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SongService.POSITION_KEY, mPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Remove the Now Playing button from this fragment
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_now_playing).setVisible(false);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            // Remove the home button and subtitle
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setSubtitle("");
            actionBar.setTitle(getString(R.string.app_name));
        }
    }


}
