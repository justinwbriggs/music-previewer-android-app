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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class ArtistListFragment extends Fragment {

    private final String LOG_TAG = ArtistListFragment.class.getSimpleName();
    public static final String EXTRA_ARTIST_ID = "artist_id"; // The artist id to pass
    public static final String EXTRA_ARTIST_NAME = "artist_name"; // The artist name to past

    private ArrayAdapter<Artist> mArtistListAdapter;
    private EditText mEdtSearch;
    private Button mBtnSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayList<Artist> artists = new ArrayList<>();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        RetainedFragment retainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());

        if(retainedFragment != null && retainedFragment.getArtistsPager() != null) {
            List<Artist> list = retainedFragment.getArtistsPager().artists.items;
            artists.addAll(list);
        }

        mArtistListAdapter = new ArtistListAdapter(getActivity(), artists);

        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

        mEdtSearch = (EditText)rootView.findViewById(R.id.edt_search);
        mBtnSearch = (Button)rootView.findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateArtistList(mEdtSearch.getText().toString());
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
        listView.setAdapter(mArtistListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Artist artist = mArtistListAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), TrackListActivity.class)
                        .putExtra(EXTRA_ARTIST_ID, artist.id)
                        .putExtra(EXTRA_ARTIST_NAME, artist.name);

                startActivity(intent);

            }
        });

        return rootView;
    }

    private void updateArtistList(String artistName) {
        FetchArtistsTask artistsTask = new FetchArtistsTask();
        artistsTask.execute(artistName);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {

                ArtistsPager results = spotify.searchArtists(params[0]);

                // Display a toast message if there are no results.
                if(results.artists.items.size() == 0) {
                    displayToast(getString(R.string.toast_no_artists));
                }

                FragmentManager fm = getActivity().getSupportFragmentManager();
                RetainedFragment retainedFragment = (RetainedFragment) fm
                        .findFragmentByTag(RetainedFragment.class.getSimpleName());


                if(retainedFragment != null) {
                    retainedFragment.setArtistsPager(results);
                }

                return results;

            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(ArtistsPager result) {

            if (result != null) {
                Pager<Artist> pager = result.artists;
                mArtistListAdapter.clear();
                for(Artist artist: pager.items) {
                    mArtistListAdapter.add(artist);
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
