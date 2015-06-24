package net.justinbriggs.android.musicpreviewer.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.justinbriggs.android.musicpreviewer.app.R;


public class TrackListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_list);

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
        if (id == R.id.action_now_playing) {

            Intent i = new Intent(getApplicationContext(),PlayerActivity.class);
            startActivity(i);

        }

        return super.onOptionsItemSelected(item);
    }

    public void setActionBarSubtitle(String subtitle) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
