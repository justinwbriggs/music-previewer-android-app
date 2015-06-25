package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.adapter.ArtistListAdapter;
import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;

//TODO: Highlight selected list item.

public class ArtistListFragment extends Fragment {

    public static final String FRAGMENT_TAG = TrackListFragment.class.getSimpleName();
    public static final String LIST_KEY = "list_key";

    public interface Listener {
        void onArtistSelected(MyArtist artist);
    }

    private Listener mListener;
    private ArrayList<MyArtist> mArtists = new ArrayList<>();
    private ArtistListAdapter mArtistListAdapter;
    private ListView mListView;
    private EditText mEdtSearch;

    public static ArtistListFragment newInstance() {
        ArtistListFragment f = new ArtistListFragment();
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO: mArtists is always null if you go to track screen, rotate twice, and come back to this screen.

        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);

        if(savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
            if(mArtists != null) {
                mArtists = savedInstanceState.getParcelableArrayList(LIST_KEY);
            }
        }

        mArtistListAdapter = new ArtistListAdapter(getActivity(), mArtists);

        rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

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

        mListView = (ListView) rootView.findViewById(R.id.listview_artist);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mListView.setAdapter(mArtistListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MyArtist artist = mArtistListAdapter.getItem(position);
                mListener.onArtistSelected(artist);
                view.setSelected(true);

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LIST_KEY, mArtists);

    }
}
