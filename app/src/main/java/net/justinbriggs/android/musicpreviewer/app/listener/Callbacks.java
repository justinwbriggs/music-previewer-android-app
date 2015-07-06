package net.justinbriggs.android.musicpreviewer.app.listener;

import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;

public interface Callbacks {
    void fragmentVisible(String fragmentTag);
    void artistSelected(MyArtist artist);
    void trackSelected(int position);
}
