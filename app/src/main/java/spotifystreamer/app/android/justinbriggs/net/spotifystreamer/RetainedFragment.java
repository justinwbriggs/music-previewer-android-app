package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import kaaes.spotify.webapi.android.models.ArtistsPager;

public class RetainedFragment extends Fragment {

    private ArtistsPager artistsPager;

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

}