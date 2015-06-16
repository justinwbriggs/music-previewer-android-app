package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;


/*
 * The reasoning behind this headless fragment is that setRetainInstance(true) will prevent the
 * fragment from being destroyed. Therefore, you can retain instance variables for orientation
 * changes in your visible fragments.
 * setRetainInstance() will not work in visible fragments since they are (usually) added to the
 * backstack, which renders this method unusable.
 */

public class RetainedFragment extends Fragment {

    private List<Artist> artists;
    private List<Track> tracks;

    // The current track being played.
    private Track track;
    private int position;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}