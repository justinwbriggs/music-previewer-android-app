package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract.TrackEntry;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;

public class PlayerDialogFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = PlayerDialogFragment.class.getSimpleName();

    public static final String POSITION_KEY = "position_key";
    public static final String FROM_ACTION_BAR_KEY = "from_action_bar_key";
    private static final int PREVIEW_DURATION = 30000;

    private SongService mBoundService;
    boolean mIsBound;


    private boolean mHasRun;
    // If the dialog was opened with the Now Playing button
    private boolean mFromActionBar;
    private int mPosition;

    // Had to create this class variable because getActivity.getContentResolver() returns null
    // when navigating back from dialog to this fragment, then clicking another track. Not sure why.
    private ContentResolver mContentResolver;

    // User is interacting with seekBar via touch/drag
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

    private Cursor mCursor;

    public static PlayerDialogFragment newInstance(int position, boolean fromActionBar) {
        PlayerDialogFragment f = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SongService.POSITION_KEY, position);
        args.putBoolean(FROM_ACTION_BAR_KEY, fromActionBar);
        f.setArguments(args);
        return f;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((SongService.LocalBinder)service).getService();

            // Only initialize player one time on startup. Prevents orientation change from firing.
            if(!mHasRun) {

                // Don't want to re-initialize player if the user resumes from action bar button.
                if(!mFromActionBar) {
                    disableButtons();

                    //Initialize player once the service is bound.
                    Intent intent = new Intent(getActivity(), SongService.class);
                    intent.setAction(SongService.ACTION_INITIALIZE_PLAYER);
                    getActivity().startService(intent);
                    // TODO: In theory this should work, but crashes with null pointer.
                    //mBoundService.init();
                    mHasRun = true;
                }
            }

            if(mFromActionBar) {
                // If dialog created from the Now Playing button, use service cursor.
                mCursor = mBoundService.getCurrentCursor();

            } else {
                // Otherwise, we're coming from the TrackListFragment. Use the db cursor.
                mCursor = mContentResolver.query(
                        MusicContract.TrackEntry.CONTENT_URI,
                        null, // leaving "columns" null just returns all the columns.
                        null, // cols for "where" clause
                        null, // values for "where" clause
                        null // Sort order
                );
                mCursor.moveToPosition(mPosition);
            }
            updatePlayPause();
            updateUi();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void bindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                SongService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void unbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bindService();
        mContentResolver = getActivity().getContentResolver();

        // Tells the host activity that the fragment has menu options it wants to manipulate.
        setHasOptionsMenu(true);
        setRetainInstance(true);

        if(getArguments() != null && getArguments().containsKey(FROM_ACTION_BAR_KEY)) {
            mFromActionBar = getArguments().getBoolean(FROM_ACTION_BAR_KEY);
        }
        // Get position from the TrackListFragment
        if(getArguments() != null && getArguments().containsKey(POSITION_KEY)) {
            mPosition = getArguments().getInt(POSITION_KEY,0);
        }
        // Get position from savedInstanceState on rotation.
        if(savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
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

        mIbPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBoundService.playPause();
            }
        });

        mIbPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBoundService.playPreviousTrack();
            }
        });

        mIbNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBoundService.playNextTrack();
            }
        });

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
                int progress = mSeekBar.getProgress();
                mBoundService.updateProgress(progress);
                mIsSeeking = false;
            }
        });

        return rootView;
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
        mTxtArtist.setText(mCursor.getString(TrackEntry.CURSOR_KEY_ARTIST_NAME));
        mTxtAlbum.setText(mCursor.getString(TrackEntry.CURSOR_KEY_ALBUM_NAME));
        try {
            // 0 should be the largest image
            //TODO: Make sure you record the thumbnail and the large image in the db
            String imageUrl = mCursor.getString(TrackEntry.CURSOR_KEY_ALBUM_IMAGE_URL);
            Picasso.with(getActivity()).load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(mIvAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTxtTrack.setText(mCursor.getString(TrackEntry.CURSOR_KEY_TRACK_NAME));
    }


    @NonNull
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
        intentFilter.addAction(SongService.BROADCAST_TRACK_PROGRESS);
        intentFilter.addAction(SongService.BROADCAST_TRACK_CHANGED);
        intentFilter.addAction(SongService.BROADCAST_PLAY_PAUSE);

        // Use LocalBroadcastManager unless you plan on receiving broadcasts from other apps.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, intentFilter);
    }

    // The play/pause button is updated when the track is changed(BROADCAST_READY), on rotation and start
    // (in ServiceConnected), and when the play/pause button is pressed.
    private void updatePlayPause() {
        if (mBoundService.isPlaying()) {
            mIbPausePlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mIbPausePlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SongService.BROADCAST_READY)) {
                // When the player is ready, update the play/pause button and enable all buttons
                updatePlayPause();
                enableButtons();
            } else if (intent.getAction().equals(SongService.BROADCAST_NOT_READY)) {
                disableButtons();
            } else if (intent.getAction().equals(SongService.BROADCAST_PLAY_PAUSE)) {
                updatePlayPause();
            } else  if(intent.getAction().equals(SongService.BROADCAST_TRACK_CHANGED)) {
                mCursor = mBoundService.getCurrentCursor();
                // Record the position for orientation changes.
                mPosition = mCursor.getPosition();
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

    @Override
    public void onDestroyView() {
        // Used to fix the disappearing dialog after rotation on tablets
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        unbindService();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: Going to have to close this?
        //mCursor.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Don't display Now Playing button in this fragment
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.action_now_playing).setVisible(false);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {

            if(getDialog() != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                // Remove the home button and subtitle
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setSubtitle("");
                actionBar.setTitle(getString(R.string.app_name));
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION_KEY, mPosition);
    }
}
