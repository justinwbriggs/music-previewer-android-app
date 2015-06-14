package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
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

public class TrackListFragment extends Fragment {

    private String mArtistId;
    private String mArtistName;
    private ArrayList<Track> mTracks  = new ArrayList<>();
    private ArrayAdapter<Track> mTrackListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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

                Track track = mTrackListAdapter.getItem(position);

                //TODO: Start the audio interface

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
            Log.v("asfd", "restoringTracks");
            mTracks = (ArrayList<Track>)retainedFragment.getTracks();
            mTrackListAdapter.clear();
            mTrackListAdapter.addAll(mTracks);
        } else {
            Log.v("asfd", "fetchingTracks");
            fetchTracks(mArtistId);
        }

    }

    public void fetchTracks(String artistId) {
        FetchTracksTask tracksTask = new FetchTracksTask();
        tracksTask.execute(artistId);
    }

    public void onResume(){
        super.onResume();

        //TODO: Handle this
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
                    displayToast(getString(R.string.toast_no_artists));
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
