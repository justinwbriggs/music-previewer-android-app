package net.justinbriggs.android.musicpreviewer.app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.fragment.PlayerDialogFragment;

/*
 * This class allows the DialogFragment to start from a notification.
 */
public class PlayerActivity extends AppCompatActivity {

    FragmentManager mFm;
    boolean mIsLargeLayout;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mFm = getSupportFragmentManager();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // Depending on the device size, dialog will either be fullscreen or floating.
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerDialogFragment.show(mFm, PlayerDialogFragment.FRAGMENT_TAG);
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = mFm.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, playerDialogFragment)
                    .addToBackStack(null).commit();
        }

    }

    //TODO: This can be removed?
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        mFm = getSupportFragmentManager();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // Depending on the device size, dialog will either be fullscreen or floating.
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerDialogFragment.show(mFm, PlayerDialogFragment.FRAGMENT_TAG);
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = mFm.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, playerDialogFragment)
                    .addToBackStack(null).commit();
        }

    }

    //TODO: This can be removed?
    @Override
    protected void onResume() {
        super.onResume();

        mFm = getSupportFragmentManager();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // Depending on the device size, dialog will either be fullscreen or floating.
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerDialogFragment.show(mFm, PlayerDialogFragment.FRAGMENT_TAG);
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = mFm.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, playerDialogFragment)
                    .addToBackStack(null).commit();
        }




    }
}
