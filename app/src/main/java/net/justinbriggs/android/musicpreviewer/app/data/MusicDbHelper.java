package net.justinbriggs.android.musicpreviewer.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.justinbriggs.android.musicpreviewer.app.data.MusicContract.TrackEntry;


/**
 * Manages a local database of music
 */
public class MusicDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "music.db";

    public MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold tracks.
        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY, " +
                TrackEntry.COLUMN_ALBUM_NAME+ " TEXT, " +
                TrackEntry.COLUMN_ARTIST_NAME+ " TEXT, " +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT, " +
                TrackEntry.COLUMN_ALBUM_IMAGE_URL + " TEXT," +
                TrackEntry.COLUMN_PREVIEW_URL + " TEXT" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

