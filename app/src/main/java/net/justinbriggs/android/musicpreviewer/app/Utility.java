package net.justinbriggs.android.musicpreviewer.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Utility {

//    public static boolean getPrefIsShownOnLockScreen(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getBoolean(context.getString(R.string.pref_is_shown_on_lock_key), false);
//    }

    public static boolean isShownOnLock(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_is_shown_on_lock_key),
                context.getString(R.string.pref_is_shown_on_lock_value_yes))
                .equals(context.getString(R.string.pref_is_shown_on_lock_value_yes));
    }

    public static String getPrefCountryCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_code_key),
                context.getString(R.string.pref_country_code_default));
    }

    public static void setCurrentTrackPositionPref(Context context, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.pref_position_key), position);
        editor.apply();

    }

    public static int getCurrentTrackPositionPref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int position = prefs.getInt(context.getString(R.string.pref_position_key), 0);
        return position;
    }

}
