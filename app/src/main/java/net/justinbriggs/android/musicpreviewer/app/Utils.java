package net.justinbriggs.android.musicpreviewer.app;


import android.content.Context;
import android.content.SharedPreferences;


public class Utils {

    private static final String PREF_KEY = "net.justinbriggs.android.musicpreviewer.app.pref_key";
    private static final String PREF_POSITION_KEY = "pref_key_position";

    public static void setCurrentTrackPositionPref(Context context, int position) {

        SharedPreferences sharedPref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_POSITION_KEY, position);
        editor.apply();

    }

    public static int getCurrentTrackPositionPref(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int position = sharedPref.getInt(PREF_POSITION_KEY, 0);
        return position;

    }

}
