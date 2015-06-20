package net.justinbriggs.android.musicpreviewer.app.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.fragment.RetainedFragment;


public class TrackListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        // Find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        RetainedFragment retainedFragment = (RetainedFragment) fm
                .findFragmentByTag(RetainedFragment.class.getSimpleName());

        // Create head
        // less RetainedFragment if it doesn't exist
        if (retainedFragment == null) {
            // add the fragment
            retainedFragment = new RetainedFragment();
            fm.beginTransaction().add(retainedFragment,
                    RetainedFragment.class.getSimpleName()).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            //startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setActionBarSubtitle(String subtitle) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

}
