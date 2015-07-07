package net.justinbriggs.android.musicpreviewer.app.listener;

import android.view.Menu;

import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;

public interface Callbacks {
    void fragmentVisible(String fragmentTag, Menu menu);
    void artistSelected(MyArtist artist);
    void trackSelected(int position);
}
