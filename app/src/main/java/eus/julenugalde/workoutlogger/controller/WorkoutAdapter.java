package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Workout;

public class WorkoutAdapter extends ArrayAdapter<Workout> {
    private Activity context;
    private List<Workout> listWorkouts;

    public WorkoutAdapter(@NonNull Activity context, int resource, @NonNull List<Workout> listWorkouts) {
        super(context, resource, listWorkouts);
        this.context = context;
        this.listWorkouts = listWorkouts;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        //TODO Replace by custom layout
        View item = inflater.inflate(R.layout.listitem_completed_training_session, null);

        Workout workout = listWorkouts.get(position);
        TextView lblName = item.findViewById(R.id.LblListItemCompletedTrainingSessionName);
        lblName.setText(workout.getName());

        TextView lblDate = item.findViewById(R.id.LblListItemCompletedTrainingSessionDate);
        lblDate.setText(workout.getNumTracks());

        TextView lblComment = item.findViewById(R.id.LblListItemCompletedTrainingSessionComment);
        lblComment.setText("XXXXX");    //TODO: Include number training sessions associated

        return item;
    }
}
