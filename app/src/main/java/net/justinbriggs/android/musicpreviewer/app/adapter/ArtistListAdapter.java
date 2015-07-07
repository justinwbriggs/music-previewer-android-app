package net.justinbriggs.android.musicpreviewer.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.justinbriggs.android.musicpreviewer.app.R;
import net.justinbriggs.android.musicpreviewer.app.model.MyArtist;

import java.util.ArrayList;


public class ArtistListAdapter extends ArrayAdapter<MyArtist> {

    private ArrayList<MyArtist> mArtists;
    private Context mContext;

    public ArtistListAdapter(Context context, ArrayList<MyArtist> artists) {
        super(context, R.layout.list_item_artist, artists);
        mArtists = artists;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyArtist artist = mArtists.get(position);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_artist, parent, false);

        if (position % 2 == 0) {
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.bg_list_item_even));
        } else {
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.bg_list_item_odd));
        }

        ImageView ivArtist = (ImageView) rowView.findViewById(R.id.iv_artist);
        try {

            // Always get the last image, which should be the 64 px size, but may not be included.
            Picasso.with(mContext).load(artist.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivArtist);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvName = (TextView) rowView.findViewById(R.id.tv_name);
        tvName.setText(artist.getName());
        return rowView;

    }

}