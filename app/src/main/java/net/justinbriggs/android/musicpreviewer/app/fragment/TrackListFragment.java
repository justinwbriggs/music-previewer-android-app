package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.Utility;
import net.justinbriggs.android.musicpreviewer.app.adapter.TrackListAdapter;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;
import net.justinbriggs.android.musicpreviewer.app.listener.Callbacks;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class TrackListFragment extends Fragment {


    public static final String FRAGMENT_TAG = TrackListFragment.class.getSimpleName();
    public static final String EXTRA_ID = "artist_id_key";
    public static final String EXTRA_NAME = "artist_name_key";

    private Callbacks mFragmentCallback;
    private String mArtistId;
    private String mArtistName;
    private TrackListAdapter mTrackListAdapter;
    private ListView mListView;

    public static TrackListFragment newInstance(String artistId, String artistName) {
        TrackListFragment f = new TrackListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ID, artistId);
        args.putString(EXTRA_NAME, artistName);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if(getArguments().containsKey(EXTRA_ID)) {
            mArtistId = getArguments().getString(EXTRA_ID);
        }
        if(getArguments().containsKey(EXTRA_NAME)) {
            mArtistName = getArguments().getString(EXTRA_NAME);
        }
        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ID)) {
            mArtistId = savedInstanceState.getString(EXTRA_ID);
        }
        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_NAME)) {
            mArtistName = savedInstanceState.getString(EXTRA_NAME);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);

        // Just pass in an empty cursor, let onViewCreated handle updating the view.
        mTrackListAdapter = new TrackListAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_track);
        mListView.setAdapter(mTrackListAdapter);

        Log.v("qwer", "tv_empty: " + rootView.findViewById(R.id.tv_empty));
                mListView.setEmptyView(rootView.findViewById(R.id.tv_empty));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Let the host activity sort out navigation.
                mFragmentCallback.trackSelected(position);
            }
        });


        return rootView;
    }

    // Once the view is created, we can populate the list of tracks.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: I wanted to cache these results so the AsyncTask did not have to run again, but
        // it was giving me issues, so I just fetch again.
        fetchTracks(mArtistId);
    }

    public void fetchTracks(String artistId) {
        mArtistId = artistId;
        FetchTracksTask tracksTask = new FetchTracksTask();
        tracksTask.execute(artistId);
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

                // An  ISO 3166-1 alpha-2 country code
                map.put("country", Utility.getPrefCountryCode(getActivity()));

                List<Track> tracks = spotify.getArtistTopTrack(params[0], map).tracks;

                // First, delete all records
                getActivity().getContentResolver().delete(
                        MusicContract.TrackEntry.CONTENT_URI,
                        null,
                        null
                );

                // Then bulk insert all records
                ContentValues[] contentValues = new ContentValues[tracks.size()];
                for(int i = 0; i < contentValues.length; i++) {

                    ContentValues trackValues = new ContentValues();
                    Track track = tracks.get(i);
                    trackValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_NAME, track.album.name);
                    trackValues.put(MusicContract.TrackEntry.COLUMN_TRACK_NAME, track.name);
                    trackValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_NAME, track.artists.get(0).name);
                    trackValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_IMAGE_URL, track.album.images.get(0).url);
                    trackValues.put(MusicContract.TrackEntry.COLUMN_PREVIEW_URL,track.preview_url);
                    contentValues[i] = trackValues;

                }

                // Add the tracks to the db
                getActivity().getContentResolver()
                        .bulkInsert(MusicContract.TrackEntry.CONTENT_URI, contentValues);

                return tracks;

            } catch(Exception e) {
                //TODO: The country code has the potential to return a 400 Bad Request.
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null) {

                // Get the saved db results
                Cursor cursor = getActivity().getContentResolver().query(
                        MusicContract.TrackEntry.CONTENT_URI,
                        null, // leaving "columns" null just returns all the columns.
                        null, // cols for "where" clause
                        null, // values for "where" clause
                        null // Sort order
                );

                mTrackListAdapter.swapCursor(cursor);

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
        try {
            mFragmentCallback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_ID, mArtistId);
        outState.putString(EXTRA_NAME, mArtistName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mFragmentCallback.fragmentVisible(FRAGMENT_TAG);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            if (SongService.sIsInitialized) {
                if (menu != null) {
                    menu.findItem(R.id.action_now_playing).setVisible(true);
                    menu.findItem(R.id.action_share).setVisible(true);
                }
            }
        }
    }
}
