package net.justinbriggs.android.musicpreviewer.app.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.Utils;
import net.justinbriggs.android.musicpreviewer.app.fragment.ArtistListFragment;
import net.justinbriggs.android.musicpreviewer.app.fragment.PlayerDialogFragment;
import net.justinbriggs.android.musicpreviewer.app.fragment.TrackListFragment;
import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;

public class MainActivity extends AppCompatActivity
        implements ArtistListFragment.Listener,
        TrackListFragment.Listener {

    //TODO: There is a full-screen click on the dialog fragment handset.

    //TODO: Figure out if we need these two.
    private boolean mTwoPane;
    private boolean mIsLargeLayout;

    // TODO: go through all the courses and add comments
    // See if you can take advantage of the manifest
    // Here's a way this might be accomplished: http://stackoverflow.com/questions/23811136

    // TODO: It's a requirement to save the selected item on rotation.
    //TODO: On handsets, if you press Now Playing and rotate, the dialog disappears. This seems to
    // only happen intermittently. Looks like it happens when you rotate while the dialog is loading?

    //TODO: Need to use startForeground for the service


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        if (findViewById(R.id.track_list_container) != null) {

            // The track_list_container container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            //Create a new TrackListFragment that initially displays nothing.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.track_list_container, TrackListFragment.newInstance(null, null), TrackListFragment.FRAGMENT_TAG)
                        .commit();
            }

        } else {

            mTwoPane = false;

            // Don't need the homeup button for large layouts
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }

            // http://stackoverflow.com/questions/27723968/
            // This is a peculiar necessity. Leaving it out will cause a fragment created in this
            // manner to fire onCreateView twice. So don't recreate it if already exists in fm.
            // It can also be accomplished with: if(fm.findFragmentByTag("theTag") != null)
            if(savedInstanceState == null) {
                loadFragment(ArtistListFragment.newInstance(), ArtistListFragment.FRAGMENT_TAG);
            }
        }
    }

    @Override
    public void onArtistSelected(MyArtist myArtist) {

        if(mTwoPane) {

            // Update the subtitle with the artist name
            if(getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(myArtist.getName());
            }

            //Update the fragment with new results.
            FragmentManager fm = getSupportFragmentManager();
            TrackListFragment trackListFragment = (TrackListFragment) fm
                    .findFragmentByTag(TrackListFragment.FRAGMENT_TAG);
            trackListFragment.fetchTracks(myArtist.getId());

        } else {
            TrackListFragment trackListFragment = TrackListFragment.newInstance(myArtist.getId(), myArtist.getName());
            loadFragment(trackListFragment, TrackListFragment.FRAGMENT_TAG);
        }

    }

    @Override
    public void onTrackSelected(int position) {

        //TODO: We should keep track of the position in a less stupid manner.
        Utils.setCurrentTrackPositionPref(getApplicationContext(), position);

        FragmentManager fm = getSupportFragmentManager();

        // Depending on the device size, dialog will either be fullscreen or floating.
        PlayerDialogFragment playerDialogFragment
                = PlayerDialogFragment.newInstance(position, false);

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerDialogFragment.show(fm, PlayerDialogFragment.FRAGMENT_TAG);
        } else {
            // Add the fragment to the backstack
            loadFragment(playerDialogFragment, PlayerDialogFragment.FRAGMENT_TAG);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void loadFragment(Fragment fragment, String tag) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.addToBackStack(tag);
        // Use add instead of replace in order to maintain the fragment view.
        ft.add(R.id.content_frame, fragment, tag);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //The action bar automatically handles clicks on Home/Up button if you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStackImmediate();
        }

        if (id == R.id.action_now_playing) {
            PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(0, true);
            if (mIsLargeLayout) {
                playerDialogFragment.show(getSupportFragmentManager(), PlayerDialogFragment.FRAGMENT_TAG);
            } else {
                loadFragment(playerDialogFragment, PlayerDialogFragment.FRAGMENT_TAG);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        FragmentManager manager = getSupportFragmentManager();
        int count = manager.getBackStackEntryCount();

        // 1 would represent the ArtistListFragment
        if(count == 1) {
            //TODO: Prompt the user to leave the app?
            finish();
        } else{
            manager.popBackStackImmediate();
        }
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



}


