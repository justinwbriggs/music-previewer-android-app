package net.justinbriggs.android.musicpreviewer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;
import net.justinbriggs.android.musicpreviewer.app.fragment.ArtistListFragment;
import net.justinbriggs.android.musicpreviewer.app.fragment.TrackListFragment;

import kaaes.spotify.webapi.android.models.Artist;

public class MainActivity extends AppCompatActivity implements ArtistListFragment.Callback {

    private boolean mTwoPane;

    // TODO: go through all the courses and add comments
    // TODO: Configure all actionbars correctly.
    // See if you can take advantage of the manifest
    // Here's a way this might be accomplished: http://stackoverflow.com/questions/23811136

    // TODO: Use ViewGroup pattern for list items
    // TODO: Need to record the selected position of the list views
    // TODO: Retain state of subtitle
    // TODO: Create an app icon, and a placeholder icon for list items

    private static final String[] TRACKS_PROJECTION = new String[] {
            MusicContract.TrackEntry.COLUMN_ARTIST_NAME,
            MusicContract.TrackEntry.COLUMN_ALBUM_NAME
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.track_list_container) != null) {

            // The track_list_container container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            //Create a new TrackListFragment that initially displays nothing.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_list_container, new TrackListFragment(), TrackListFragment.FRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false;
            if(getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0f);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Only called:
     * 1. After Activity recreation from orientation change.
     * 2. When returning to this activity from another Activity after this activity has been killed
     * via memory manager.
     *
     * NOT Called when:
     * 1. Application is first started.
     * 2. Returning to this activity from another Activity via Back button, and this activity is
     * currently in a stop state.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /*
     * Called when:
     * 1. A new Activity is started, since this activity would be subject to destruction via
     * memory management.
     * 2. On Orientation change, since it destroys this activity.
     * NOT called when:
     * 1. Pressing the back button from this activity.
     */
    // We're currently just letting the fragments handle their own state.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(Artist artist) {
        if(mTwoPane) {

            //Update the fragment with new results.
            FragmentManager fm = getSupportFragmentManager();
            TrackListFragment trackListFragment = (TrackListFragment) fm
                    .findFragmentByTag(TrackListFragment.FRAGMENT_TAG);
            trackListFragment.fetchTracks(artist.id);

            // Set the subtitle
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(artist.name);
            }

        } else {

            //Start a new TrackListActivity
            Intent intent = new Intent(getApplicationContext(), TrackListActivity.class)
                    .putExtra(ArtistListFragment.EXTRA_ARTIST_ID, artist.id)
                    .putExtra(ArtistListFragment.EXTRA_ARTIST_NAME, artist.name);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


