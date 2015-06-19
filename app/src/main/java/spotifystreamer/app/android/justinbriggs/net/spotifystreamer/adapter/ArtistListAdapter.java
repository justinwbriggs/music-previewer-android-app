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
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import spotifystreamer.app.android.justinbriggs.net.spotifystreamer.R;


public class ArtistListAdapter extends ArrayAdapter<Artist> {

    private ArrayList<Artist> mArtists;
    private Context mContext;

    public ArtistListAdapter(Context context, ArrayList<Artist> artists) {
        super(context, R.layout.list_item_artist, artists);
        mArtists = artists;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Artist artist = mArtists.get(position);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_artist, parent, false);

        ImageView ivArtist = (ImageView) rowView.findViewById(R.id.iv_artist);
        try {

            List<Image> images = artist.images;

            // Always get the last image, which should be the 64 px size, but may not be included.
            Picasso.with(mContext).load(images.get(images.size()-1).url)
                    .placeholder(R.drawable.ic_launcher)
                    .into(ivArtist);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvName = (TextView) rowView.findViewById(R.id.tv_name);
        tvName.setText(artist.name);
        return rowView;

    }

}