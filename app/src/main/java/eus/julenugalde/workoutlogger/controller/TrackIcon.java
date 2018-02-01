package eus.julenugalde.workoutlogger.controller;

import eus.julenugalde.workoutlogger.R;

/** Helper class that manages the assignment of image resources to tracks */
public class TrackIcon {

    /** Assigns an icon based on the track name
     *
     * @param text track name
     * @return Resource id
     */
    public static int getResourceId(String text) {
        //TODO Replace strings by elements in a resource file
        /*TypedArray defaultTracks =
                getContext().getResources().obtainTypedArray(R.array.defaultTracks);
        TypedArray defaultTrackIcons =
                getContext().getResources().obtainTypedArray(R.array.defaultTrackIcons);
        for (int i=0; i<defaultTracks.length(); i++) {
            if (text.equals(defaultTracks.getNonResourceString(i))) {
                imgIcon.setImageResource(defaultTrackIcons.getResourceId(i, R.drawable.track_default));
            }
        }*/
        if (text.equals("Calentamiento"))
            return R.drawable.track_warmup;
        else if (text.equals("Squad"))
            return R.drawable.track_squats;
        else if (text.equals("Pectoral"))
            return R.drawable.track_chest;
        else if (text.equals("Espalda"))
            return R.drawable.track_back;
        else if (text.equals("Triceps"))
            return R.drawable.track_triceps;
        else if (text.equals("Biceps"))
            return R.drawable.track_biceps;
        else if (text.equals("Hombro"))
            return R.drawable.track_shoulders;
        else if (text.equals("Lunge"))
            return R.drawable.track_lunges;
        else if (text.equals("Abdominal"))
            return R.drawable.track_core;
        else
            return R.drawable.track_default;
    }
}
