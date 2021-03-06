package eus.julenugalde.workoutlogger.controller;

import android.content.Context;
import eus.julenugalde.workoutlogger.R;

/** Helper class that manages the assignment of image resources to tracks */
public class TrackIcon {
    private static final String TAG = TrackIcon.class.getSimpleName();
    private static String[][] defaultTrackNames = null;
    private static String[] locales;

    /** Assigns an icon based on the track name
     *
     * @param text track name
     * @return Resource id
     */
    public static int getResourceId(String text, Context context) {
        if (defaultTrackNames == null) {    //first call. Fill array with default track names
            locales = LocalesManager.getLocales();
            defaultTrackNames = LocalesManager.getDefaultTrackNames();
        }
        for (int i=0; i<locales.length; i++) {
            if (text.equalsIgnoreCase(defaultTrackNames[i][0])) {
                return R.drawable.track_warmup;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][1])) {
                return R.drawable.track_squats;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][2])) {
                return R.drawable.track_chest;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][3])) {
                return R.drawable.track_back;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][4])) {
                return R.drawable.track_triceps;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][5])) {
                return R.drawable.track_biceps;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][6])) {
                return R.drawable.track_lunges;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][7])) {
                return R.drawable.track_shoulders;
            }
            else if (text.equalsIgnoreCase(defaultTrackNames[i][8])) {
                return R.drawable.track_core;
            }
        }
        return R.drawable.track_default;    //By default, if not found
    }
}
