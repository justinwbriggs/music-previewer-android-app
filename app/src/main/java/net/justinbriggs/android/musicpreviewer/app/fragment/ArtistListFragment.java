package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.adapter.ArtistListAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

//TODO: Highlight selected list item.

public class ArtistListFragment extends Fragment {

    public interface Callback {
        void onItemSelected(Artist artist);
    }

    public static final String EXTRA_ARTIST_ID = "artist_id"; // The artist id to pass
    public static final String EXTRA_ARTIST_NAME = "artist_name"; // The artist name to past

    private ArrayList<Artist> mArtists = new ArrayList<>();
    private ArrayAdapter<Artist> mArtistListAdapter;
    private ListView mListView;
    private EditText mEdtSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
                    fetchArtists(mEdtSearch.getText().toString());
                }
                // Closes the keyboard
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mEdtSearch.getWindowToken(), 0);
                return false;
            }
        });


        // Let onResume take care of populating the list view, since it is called when both
        // the device is reoriented, when returning from another activity via the Back button,
        // and when returning from another activity via the Up button
        mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtists);

        mListView = (ListView) rootView.findViewById(R.id.listview_artist);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mListView.setAdapter(mArtistListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Artist artist = mArtistListAdapter.getItem(position);
                ((Callback)getActivity()).onItemSelected(artist);
                view.setSelected(true);




            }
        });

        return rootView;
    }

    @Override
    public void onResume() {


//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        RetainedFragment retainedFragment = (RetainedFragment) fm
//                .findFragmentByTag(RetainedFragment.class.getSimpleName());
//
//        if(retainedFragment != null && retainedFragment.getArtists() != null) {
//            mArtists = (ArrayList<Artist>)retainedFragment.getArtists();
//            mArtistListAdapter.clear();
//            mArtistListAdapter.addAll(mArtists);
//        }

        super.onResume();
    }

    private void fetchArtists(String artistName) {
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

                // Retain the

                // Display a toast message if there are no results.
                if(results.artists.items.size() == 0) {
                    displayToast(getString(R.string.toast_no_artists));
                }



                // Set RetainedFragment values for managing instance state.
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                RetainedFragment retainedFragment = (RetainedFragment) fm
//                        .findFragmentByTag(RetainedFragment.class.getSimpleName());
//                if(retainedFragment != null) {
//                    retainedFragment.setArtists(results.artists.items);
//                }



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
