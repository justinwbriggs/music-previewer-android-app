package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class ArtistListFragment extends Fragment {

    public static final String EXTRA_ARTIST_ID = "artist_id"; // The artist id to pass
    public static final String EXTRA_ARTIST_NAME = "artist_name"; // The artist name to past

    private ArrayAdapter<Artist> mArtistListAdapter;
    private EditText mEdtSearch;

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
        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                // IME_ACTION_SEARCH defines the keyboard and landscape submit buttons.
                if (i == EditorInfo.IME_ACTION_SEARCH) {

                    if(mEdtSearch.getText().toString().equalsIgnoreCase("")) {
                        displayToast(getString(R.string.toast_artist_required));
                        // Keeps the keyboard visible
                        return true;
                    }
                    updateArtistList(mEdtSearch.getText().toString());
                }
                // Closes the keyboard
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mEdtSearch.getWindowToken(), 0);
                return false;
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

                // Set RetainedFragment values for managing instance state.
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
