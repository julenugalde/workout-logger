package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingExercise;

/** Provides views for a ListView of {@link eus.julenugalde.workoutlogger.model.Track}
 * elements, reusing the listitem_training_exercise layout
 */
public class TrackAdapter extends ArrayAdapter<Track> {
    Activity context;
    ArrayList<Track> listTracks;

    /** Constructor that gets a list of {@link eus.julenugalde.workoutlogger.model.Track}
     * objects to be displayed in a ListView
     *
     * @param context The current context
     * @param listTracks List of {@link Track} elements to be displayed
     */
    public TrackAdapter(Activity context, ArrayList<Track> listTracks) {
        super(context, R.layout.listitem_training_exercise, listTracks);
        this.context = context;
        this.listTracks = listTracks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        //the training exercise layout is reused
        View item = inflater.inflate(R.layout.listitem_training_exercise, null);
        Track track = listTracks.get(position);
        String name = track.getName();

        //Capture the controls and fill them
        TextView lblName = (TextView)item.findViewById(R.id.LblListItemTrainingExerciseName);
        lblName.setText(name);

        ImageView imgIcon = (ImageView)item.findViewById(R.id.ImgListItemTrainingExerciseIcon);
        imgIcon.setImageResource(TrackIcon.getResourceId(name, context));

        TextView lblLoads = (TextView)item.findViewById(R.id.LblListItemTrainingExerciseLoads);
        if (track.getNumLoads() == 0) {
            lblLoads.setText(R.string.training_session_detail_no_load);
        }
        else {
            StringBuilder sb = new StringBuilder();
            Load[] loads = track.getLoads();
            for (Load load : loads) {
                if (!load.getName().isEmpty()) {
                    sb.append(load.getName() + "\n");
                }
            }
            lblLoads.setText(sb.substring(0, sb.length()-1));   //Remove the final line feed
        }
        return item;
    }
}
