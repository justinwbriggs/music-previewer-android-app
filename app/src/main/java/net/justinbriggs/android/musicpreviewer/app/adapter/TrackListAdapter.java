package net.justinbriggs.android.musicpreviewer.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.justinbriggs.android.musicpreviewer.app.R;

public class TrackListAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a track list item.
     */
    public static class ViewHolder {
        public final ImageView ivAlbum;
        public final TextView tvAlbum;
        public final TextView tvTrack;

        public ViewHolder(View view) {
            ivAlbum = (ImageView) view.findViewById(R.id.iv_album);
            tvAlbum = (TextView) view.findViewById(R.id.tv_album);
            tvTrack = (TextView) view.findViewById(R.id.tv_track);
        }
    }

    public TrackListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_track, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        try {
            // The index represents url for 64 px image.
            Picasso.with(mContext).load(cursor.getString(4))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(viewHolder.ivAlbum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewHolder.tvAlbum.setText(cursor.getString(1));
        viewHolder.tvTrack.setText(cursor.getString(3));

    }

}

