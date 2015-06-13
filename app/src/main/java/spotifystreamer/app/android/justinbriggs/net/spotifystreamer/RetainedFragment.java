package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;


/*
 * The reasoning behind this headless fragment is that setRetainInstance(true) will prevent the
 * fragment from being destroyed. Therefore, you can retain instance variables for storage on
 * configuration changes. Effectively, onDestroy() is not called, although all other methods, including
 * onCreateView is called, redrawing the view. setRetainInstance() is not
 *
 * Technically, should we be saving an instance of this fragment in onSaveInstanceState()?
 * Can we share this among different activities?
 * Remember, onDestroy does not actually kill an activity
 */

public class RetainedFragment extends Fragment {

    private ArtistsPager artistsPager;
    private List<Track> tracks;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);

    }

    public void setArtistsPager(ArtistsPager artistsPager) {
        this.artistsPager = artistsPager;
    }

    public ArtistsPager getArtistsPager() {
        return artistsPager;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}