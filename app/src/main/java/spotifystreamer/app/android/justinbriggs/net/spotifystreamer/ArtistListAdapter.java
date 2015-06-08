package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;


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

        ImageView ivFriend = (ImageView) rowView.findViewById(R.id.iv_artist);
        try {
            // The index represents url for 64 px image.
            Picasso.with(mContext).load(artist.images.get(2).url)
                    .placeholder(R.drawable.ic_launcher)
                    .into(ivFriend);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvName = (TextView) rowView.findViewById(R.id.tv_name);
        tvName.setText(artist.name);
        return rowView;

    }

}