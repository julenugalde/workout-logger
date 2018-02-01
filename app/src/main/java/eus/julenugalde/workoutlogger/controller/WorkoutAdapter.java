package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Workout;

/** Provides views for a ListView of {@link eus.julenugalde.workoutlogger.model.Workout}
 * elements, using the listitem_workout layout
 */
public class WorkoutAdapter extends ArrayAdapter<Workout> {
    private Activity context;
    private List<Workout> listWorkouts;

    /** Constructor that gets a list of {@link eus.julenugalde.workoutlogger.model.Workout}
     * objects to be displayed in a ListView
     *
     * @param context The current context
     * @param listWorkouts List of {@link Workout} elements to be displayed
     */
    public WorkoutAdapter(@NonNull Activity context, @NonNull List<Workout> listWorkouts) {
        super(context, R.layout.listitem_workout, listWorkouts);
        this.context = context;
        this.listWorkouts = listWorkouts;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View item = inflater.inflate(R.layout.listitem_workout, null);

        Workout workout = listWorkouts.get(position);
        TextView lblName = item.findViewById(R.id.LblListItemWorkoutName);
        lblName.setText(workout.getName());

        TextView lblNumTracks = item.findViewById(R.id.LblListItemWorkoutNumTracks);
        lblNumTracks.setText(workout.getNumTracks() + " " +
                getContext().getResources().getString(R.string.list_workouts_tracks));

        TextView lblNumSessions = item.findViewById(R.id.LblListItemWorkoutNumSessions);
        int numTrainingSessions = workout.getNumTrainingSessions();
        lblNumSessions.setText(numTrainingSessions + " " +
                getContext().getResources().getString(R.string.list_workouts_completed_sessions));

        ImageButton imgViewTracks = (ImageButton)item.findViewById(R.id.ImgListItemWorkoutViewTracks);
        imgViewTracks.setOnClickListener(
                new WorkoutListener(item, workout, WorkoutListener.ACTION_VIEW_TRACKS));

        ImageButton imgDelete = (ImageButton)item.findViewById(R.id.ImgListItemWorkoutDelete);
        imgDelete.setOnClickListener(
                new WorkoutListener(item, workout, WorkoutListener.ACTION_DELETE));

        return item;
    }
}
