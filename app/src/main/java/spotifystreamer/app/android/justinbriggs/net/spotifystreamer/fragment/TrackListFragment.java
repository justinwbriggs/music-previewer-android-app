package spotifystreamer.app.android.justinbriggs.net.spotifystreamer.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.R;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.activity.TrackListActivity;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.adapter.TrackListAdapter;

public class TrackListFragment extends Fragment {

    private static final String PLAYER_DIALOG_FRAGMENT_TAG = "PDFTAG";

    private String mArtistId;
    private String mArtistName;
    private ArrayList<Track> mTracks  = new ArrayList<>();
    private ArrayAdapter<Track> mTrackListAdapter;
    boolean mIsLargeLayout;
    FragmentManager mFm;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFm = getActivity().getSupportFragmentManager();

        setHasOptionsMenu(true);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // If the Activity was launched in OnePane mode, get the intent args.
        Intent intent = getActivity().getIntent();

        if(intent != null && intent.hasExtra(ArtistListFragment.EXTRA_ARTIST_ID)) {
            mArtistId = intent.getStringExtra(ArtistListFragment.EXTRA_ARTIST_ID);
        }
        if(intent != null && intent.hasExtra(ArtistListFragment.EXTRA_ARTIST_NAME)) {
            mArtistName = intent.getStringExtra(ArtistListFragment.EXTRA_ARTIST_NAME);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);

        // Just pass in an empty arraylist, let onViewCreated handle updating the view.
        mTrackListAdapter = new TrackListAdapter(getActivity(), mTracks);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_track);
        listView.setAdapter(mTrackListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                // Save the current position in RetainedFragment
                RetainedFragment retainedFragment = (RetainedFragment) mFm
                        .findFragmentByTag(RetainedFragment.class.getSimpleName());
                retainedFragment.setPosition(position);

                // We handle displaying the dialog fragment here instead of using a Callback, since
                // the host activity may not exist.

                // Depending on the device size, dialog will either be fullscreen or floating.
                PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();

                if (mIsLargeLayout) {
                    // The device is using a large layout, so show the fragment as a dialog
                    playerDialogFragment.show(mFm, PLAYER_DIALOG_FRAGMENT_TAG);
                } else {
                    // The device is smaller, so show the fragment fullscreen
                    FragmentTransaction transaction = mFm.beginTransaction();
                    // For a little polish, specify a transition animation
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    // To make it fullscreen, use the 'content' root view as the container
                    // for the fragment, which is always the root view for the activity
                    transaction.add(android.R.id.content, playerDialogFragment)
                            .addToBackStack(null).commit();
                }

            }
        });

        return rootView;
    }

    // Once the view is created, we can populate the list of tracks.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        RetainedFragment retainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());

        if(retainedFragment != null && retainedFragment.getTracks() != null) {
            mTracks = (ArrayList<Track>)retainedFragment.getTracks();
            mTrackListAdapter.clear();
            mTrackListAdapter.addAll(mTracks);
        } else {
            fetchTracks(mArtistId);
        }

    }

    public void fetchTracks(String artistId) {
        FetchTracksTask tracksTask = new FetchTracksTask();
        tracksTask.execute(artistId);
    }

    public void onResume(){
        super.onResume();

        // Set subtitle
        if(getActivity().getActionBar() != null){
            ((TrackListActivity) getActivity())
                    .setActionBarSubtitle(mArtistName);
        }

    }

    public class FetchTracksTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {

                Map<String, Object> map = new HashMap<>();
                map.put("country", Locale.getDefault().getCountry());

                List<Track> tracks = spotify.getArtistTopTrack(params[0], map).tracks;
                if(tracks.size() == 0) {
                    displayToast(getString(R.string.toast_no_tracks));
                }

                // Set RetainedFragment values for managing instance state.
                FragmentManager fm = getActivity().getSupportFragmentManager();
                RetainedFragment retainedFragment = (RetainedFragment) fm
                        .findFragmentByTag(RetainedFragment.class.getSimpleName());
                if(retainedFragment != null) {
                    retainedFragment.setTracks(tracks);
                }

                return tracks;

            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null) {
                mTrackListAdapter.clear();
                for(Track track: tracks) {
                    mTrackListAdapter.add(track);
                }
            }
        }
    }

    protected void displayToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
