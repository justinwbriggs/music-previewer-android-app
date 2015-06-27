package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.adapter.ArtistListAdapter;
import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;

public class ArtistListFragment extends Fragment {

    public static final String FRAGMENT_TAG = TrackListFragment.class.getSimpleName();
    public static final String LIST_KEY = "list_key";
    public static final String POSITION_KEY = "position_key";

    public interface Listener {
        void onArtistSelected(MyArtist artist);
    }

    private Listener mListener;
    private ArrayList<MyArtist> mArtists = new ArrayList<>();
    private ArtistListAdapter mArtistListAdapter;
    private ListView mListView;
    private EditText mEdtSearch;
    private int mPosition;


    public static ArtistListFragment newInstance() {
        ArtistListFragment f = new ArtistListFragment();
        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        // After re-orientation, set the last position
        mListView.setSelection(mPosition);
        //TODO: Can't figure out how to highlight the last selected item on rotation
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);
        if(savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
            mArtists = savedInstanceState.getParcelableArrayList(LIST_KEY);
        }

        mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtists);

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

        mListView = (ListView) rootView.findViewById(R.id.listview_artist);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mListView.setAdapter(mArtistListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MyArtist artist = mArtistListAdapter.getItem(position);
                mListener.onArtistSelected(artist);
                view.setSelected(true);
                // This records the artist list position.
                mPosition = position;

            }
        });

        return rootView;
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

                // Display a toast message if there are no results.
                if(results.artists.items.size() == 0) {
                    displayToast(getString(R.string.toast_no_artists));
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

                    //TODO: Make this a static method in the MyArtist class

                    // Turning each Spotify Artist into a parcelable MyArtist
                    MyArtist myArtist = new MyArtist();
                    myArtist.setId(artist.id);
                    myArtist.setName(artist.name);
                    List<Image> images = artist.images;
                    if(images.size() > 0) {
                        myArtist.setImageUrl(images.get(images.size()-1).url);
                    }
                    mArtistListAdapter.add(myArtist);

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LIST_KEY, mArtists);
        outState.putInt(POSITION_KEY, mPosition);
    }

    // This gets called when the activity is resumed because we set hasOptionsMenu(true)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            if(getResources().getBoolean(R.bool.large_layout)) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                // Remove the home button and subtitle
                actionBar.setTitle(getString(R.string.app_name));
                actionBar.setSubtitle("");
            }
            if (SongService.sIsInitialized) {
                if (menu != null) {
                    menu.findItem(R.id.action_now_playing).setVisible(true);
                }
            }
        }
    }

}
