package spotifystreamer.app.android.justinbriggs.net.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.R;


public class TrackListAdapter extends ArrayAdapter<Track> {

    private ArrayList<Track> mTracks;
    private Context mContext;

    public TrackListAdapter(Context context, ArrayList<Track> tracks) {
        super(context, R.layout.list_item_track, tracks);
        mTracks = tracks;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Track track = mTracks.get(position);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_track, parent, false);

        ImageView ivAlbum = (ImageView) rowView.findViewById(R.id.iv_album);
        try {
            // The index represents url for 64 px image.
            Picasso.with(mContext).load(track.album.images.get(2).url)
                    .placeholder(R.drawable.ic_launcher)
                    .into(ivAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvAlbum = (TextView)rowView.findViewById(R.id.tv_album);
        tvAlbum.setText(track.album.name);

        TextView tvName = (TextView) rowView.findViewById(R.id.tv_track);
        tvName.setText(track.name);

        return rowView;

    }

}