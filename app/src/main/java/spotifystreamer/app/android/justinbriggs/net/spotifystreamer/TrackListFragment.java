package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    private final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    private String mArtistId;
    private String mArtistName;
    private ArrayList<Track> mTracks;
    private ArrayAdapter<Track> mTrackListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();

        if(intent != null && intent.hasExtra(ArtistListFragment.EXTRA_ARTIST_ID)) {
            mArtistId = intent.getStringExtra(ArtistListFragment.EXTRA_ARTIST_ID);
        }
        if(intent != null && intent.hasExtra(ArtistListFragment.EXTRA_ARTIST_NAME)) {
            mArtistName = intent.getStringExtra(ArtistListFragment.EXTRA_ARTIST_NAME);
        }


        // So we update the UI until the AsyncTask is finished. It would be better to just
        // pass in an empty array and return the view, than
        //populateTrackAdapter();


        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);

        mTracks = new ArrayList<>();
        mTrackListAdapter = new TrackListAdapter(getActivity(),mTracks);
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
            List<Track> list = retainedFragment.getTracks();
            mTracks.addAll(list);
        } else {
            fetchTracks(mArtistId);
        }

    }

    private void fetchTracks(String artist) {
        FetchTracksTask tracksTask = new FetchTracksTask();
        tracksTask.execute(artist);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void onResume(){
        super.onResume();

        // Set subtitle
        ((TrackListActivity) getActivity())
                .setActionBarSubtitle(mArtistName);

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
