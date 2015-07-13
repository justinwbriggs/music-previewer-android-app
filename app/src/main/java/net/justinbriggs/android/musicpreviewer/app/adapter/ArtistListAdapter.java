package net.justinbriggs.android.musicpreviewer.app.adapter;

import android.content.Context;
import android.graphics.Color;
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
    public View getView(int position, View view, ViewGroup parent) {

        MyArtist artist = mArtists.get(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_artist, parent, false);
        }


        ImageView ivArtist = (ImageView) view.findViewById(R.id.iv_artist);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);

        tvName.setText(artist.getName());
        ivArtist.setBackgroundColor(Color.TRANSPARENT);
        try {
            // Always get the last image, which should be the 64 px size, but may not be included.
            Picasso.with(mContext).load(artist.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivArtist);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;

    }

}