package net.justinbriggs.android.musicpreviewer.app.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.adapter.TrackListAdapter;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class TrackListFragment extends Fragment {

    public interface Listener {
        void onTrackSelected(int position);
    }

    public static final String FRAGMENT_TAG = TrackListFragment.class.getSimpleName();
    public static final String EXTRA_ID = "artist_id_key";
    public static final String EXTRA_NAME = "artist_name_key";

    private Listener mListener;
    private String mArtistId;
    private String mArtistName;
    private TrackListAdapter mTrackListAdapter;

    public static TrackListFragment newInstance(String artistId, String artistName) {

        //TODO: We'll be passing the position off to the SongService for now, but keeping it in sync
        // is going to be an issue. Might want to see if you can just allow for this dialog to
        // get the current position from the SongService.
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
        ListView listView = (ListView) rootView.findViewById(R.id.listview_track);
        listView.setAdapter(mTrackListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Let the host activity sort out navigation.
                mListener.onTrackSelected(position);
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
                map.put("country", Locale.getDefault().getCountry());

                List<Track> tracks = spotify.getArtistTopTrack(params[0], map).tracks;
                if(tracks.size() == 0) {
                    displayToast(getString(R.string.toast_no_tracks));
                }
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



                //TODO: So here is where I'm getting screwed up. Every time I click on an artist and
                // the track list reloads, I'm saving it in the db

                // TODO: Check to see if these results are the same as the

                // Add the tracks to the db
                getActivity().getContentResolver()
                        .bulkInsert(MusicContract.TrackEntry.CONTENT_URI, contentValues);

                return tracks;

            } catch(Exception e) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main, menu);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            // Remove the home button and subtitle
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(mArtistName);
            if(SongService.sIsInitialized) {
                    if(menu != null) {
                        menu.findItem(R.id.action_now_playing).setVisible(true);
                    }
                }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_ID, mArtistId);
        outState.putString(EXTRA_NAME, mArtistName);

    }
}
