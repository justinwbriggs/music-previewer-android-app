package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    private String mArtistId;
    private String mArtistName;
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

        mTrackListAdapter = new TrackListAdapter(getActivity(), new ArrayList<Track>());

        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);


        ListView listView = (ListView) rootView.findViewById(R.id.listview_track);
        listView.setAdapter(mTrackListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Track track = mTrackListAdapter.getItem(position);
                Log.v(LOG_TAG, "artistName: " + track.name);

                //TODO: Start the audio interface



            }
        });

        return rootView;
    }

    private void updateTrackList(String artist) {
        FetchTracksTask artistsTask = new FetchTracksTask();
        artistsTask.execute(artist);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrackList(mArtistId);
    }

    public void onResume(){
        super.onResume();

        // Set subtitle
        ((TrackListActivity) getActivity())
                .setActionBarSubtitle(mArtistName);

    }

    public class FetchTracksTask extends AsyncTask<String, Void, List<Track>> {

        private final String LOG_TAG = FetchTracksTask.class.getSimpleName();

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
