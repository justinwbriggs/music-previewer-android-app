package net.justinbriggs.android.musicpreviewer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.Utility;
import net.justinbriggs.android.musicpreviewer.app.fragment.ArtistListFragment;
import net.justinbriggs.android.musicpreviewer.app.fragment.PlayerDialogFragment;
import net.justinbriggs.android.musicpreviewer.app.fragment.TrackListFragment;
import net.justinbriggs.android.musicpreviewer.app.listener.Callbacks;
import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;
import net.justinbriggs.android.musicpreviewer.app.service.SongService;


public class MainActivity extends AppCompatActivity
        implements Callbacks {

    public static final String EXTRA_ARTIST = "artist_key";

    private boolean mTwoPane;
    private boolean mIsLargeLayout;

    private ShareActionProvider mShareActionProvider;

    // Used to keep the action bar updated on large layouts
    private MyArtist mArtist;

    //TODO: Additional Features:

//    Caches queries/results so that any recent query can be repeated without a call to the Spotify API.
//    Allows user to see cached Artist/Track info even when off line.
//    Cache cleans up stale data to keep its size reasonable.
//    Pre-fetch the track data for the top few artist results.
//    Batches network transactions for better performance.
//    Added a preference setting to filter tracks marked as Explicit.
//    Created custom launcher and notification icons.
//    Begins playing the next song in the list when the current song completes.
//    The currently playing track is highlighted in the track list. This is updated when it moves to the next track.
//    Notification data is also updated when the next track begins.

    //I'm using the SQLite database to store the cache data. There are 4 tables. Query, QueryArtist,
    // Artist, and Track. Query stores strings that the user has searched for, Artist and Track
    // store that data, and QueryArtist allows a many-to-many join between queries and artists
    // (1 query results in multiple artists, and each artist may be found via multiple queries).
    // The Artist List and Track List components use a ContentProvider to pull their data directly
    // from the database.

    
    //TODO: On handsets, if you press Now Playing and rotate, the dialog disappears. This seems to
    // only happen intermittently. Looks like it happens when you rotate while the dialog is loading?
    //Test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The Theme's windowBackground is masked by the opaque background of the activity, and
        // the windowBackground causes an unnecessary overdraw. Nullifying the windowBackground
        // removes that overdraw.
        getWindow().setBackgroundDrawable(null);

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
    public void artistSelected(MyArtist myArtist) {

        // Keep track of the artist for rotation purposes.
        mArtist = myArtist;

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
    public void trackSelected(int position) {

        //TODO: We should keep track of the position in a less stupid manner.
        Utility.setCurrentTrackPositionPref(getApplicationContext(), position);

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

    private void loadFragment(Fragment fragment, String tag) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.addToBackStack(tag);
        // Use add instead of replace in order to maintain the fragment view.
        ft.add(R.id.content_frame, fragment, tag);
        ft.commit();
    }


    // The main controls for the actionbar. Each fragment invokes a callback to alert the activity
    // that they are visible. The method is invoked in onCreateOptionsMenu() since onResume is not
    // called in the fragment when the backstack is popped. I also tried to handle the actionBar from
    // onAttachFragment(), but it was called before setContentView() on some occasions, rendering
    // the actionBar null.
    @Override
    public void fragmentVisible(String fragmentTag, Menu menu) {

        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null || menu == null) {
            return;
        }

        if(!mTwoPane) {
            if (fragmentTag.equals(ArtistListFragment.FRAGMENT_TAG)) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                // Remove the home button and subtitle
                actionBar.setTitle(getString(R.string.app_name));
                actionBar.setSubtitle("");
                if (SongService.sIsInitialized) {
                    menu.findItem(R.id.action_now_playing).setVisible(true);
                    menu.findItem(R.id.action_share).setVisible(true);
                }

            } else if(fragmentTag.equals(TrackListFragment.FRAGMENT_TAG)) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setSubtitle(mArtist.getName());
                actionBar.setTitle(getString(R.string.title_track_list));
                if (SongService.sIsInitialized) {
                    menu.findItem(R.id.action_now_playing).setVisible(true);
                    menu.findItem(R.id.action_share).setVisible(true);

                }

            } else if(fragmentTag.equals(PlayerDialogFragment.FRAGMENT_TAG)) {
                // Remove the home button and subtitle
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setSubtitle("");
                actionBar.setTitle(getString(R.string.app_name));
                // Don't display Now Playing button in this fragment
                menu.findItem(R.id.action_now_playing).setVisible(false);

            }
        } else {
            // Reset the artist name on rotation for large layouts.
            if(mArtist != null) {
                actionBar.setSubtitle(mArtist.getName());
            }
            if (SongService.sIsInitialized) {
                menu.findItem(R.id.action_now_playing).setVisible(true);
                menu.findItem(R.id.action_share).setVisible(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStackImmediate();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_now_playing:
                PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(0, true);
                if (mIsLargeLayout) {
                    playerDialogFragment.show(getSupportFragmentManager(), PlayerDialogFragment.FRAGMENT_TAG);
                } else {
                    loadFragment(playerDialogFragment, PlayerDialogFragment.FRAGMENT_TAG);
                }
                return true;
            case R.id.action_share:
                //TODO Non-critical: I can't get the share button to appear as an icon, because
                // the shareIntent must be set onCreateOptionsMenu. It's in the overflow menu for now.
                if(mShareActionProvider == null) {
                    setShareIntent(item);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setShareIntent(MenuItem item) {

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // We're only technically required to share the url, and some apps only accept a url as EXTRA_TEXT
        if(SongService.sUrl != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, SongService.sUrl);
        }
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onBackPressed() {

        FragmentManager manager = getSupportFragmentManager();
        int count = manager.getBackStackEntryCount();

        // 1 would represent the ArtistListFragment
        if(count == 1) {
            finish();
        } else{
            manager.popBackStackImmediate();
        }
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ARTIST)) {
            mArtist = savedInstanceState.getParcelable(EXTRA_ARTIST);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ARTIST, mArtist);
    }

}


