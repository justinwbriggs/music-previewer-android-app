package net.justinbriggs.android.musicpreviewer.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;




/**
 * Defines table and column names for the music database.
 */
public class MusicContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "net.justinbriggs.android.musicpreviewer.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider. It is a unique string used to locate your content.
    // content:// is called the scheme.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://net.justinbriggs.android.musicpreviewer.app/track/ is a valid path for
    // looking at track data. content://net.justinbriggs.android.musicpreviewer.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_TRACK = "track";


    /* Inner class that defines the table contents of the track table */
    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build();

        // Cursors returned from a content provider have unique types based on their content
        // and the base path used for the query. Android used a form similar to a MIME type to
        // describe the type of content returned by the URI. Cursors that can return more than one
        // item are prefixed with CURSOR_DIR_BASE_TYPE. Cursors that return a single item are prefixed
        // with CONTENT_ITEM_BASE_TYPE.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;

        // Table names
        public static final String TABLE_NAME = "track";

        public static final String COLUMN_ALBUM_NAME = "album_name";
        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_ALBUM_IMAGE_URL = "album_image_url";
        public static final String COLUMN_PREVIEW_URL = "preview_url";

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
