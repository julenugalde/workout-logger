package eus.julenugalde.workoutlogger.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** Class that extends {@link android.widget.ArrayAdapter<Track>} in order to show the track
 * names in a spinner of {@link eus.julenugalde.workoutlogger.view.ActivityViewStatistics}. The
 * base class can't be used because it automatically call the toString() method of {@link Track}
 * instead of the required getName().
 */
import eus.julenugalde.workoutlogger.model.Track;

public class TrackArrayAdapter extends ArrayAdapter<Track> {

    public TrackArrayAdapter(@NonNull Context context, int resource, @NonNull Track[] objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView)super.getView(position, convertView, parent);
        view.setText(getItem(position).getName());
        return view;
    }

    
}
