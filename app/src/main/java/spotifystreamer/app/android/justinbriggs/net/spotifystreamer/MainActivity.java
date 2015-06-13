package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    //TODO: Retained fragment works with the Back button, but not with the Up button.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        RetainedFragment retainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());

        // Create headless RetainedFragment if it doesn't exist
        if (retainedFragment == null) {
            // add the fragment
            retainedFragment = new RetainedFragment();
            fm.beginTransaction().add(retainedFragment,
                    RetainedFragment.class.getSimpleName()).commit();
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
    protected void onDestroy() {
        super.onDestroy();
    }
}


