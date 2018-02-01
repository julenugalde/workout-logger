package eus.julenugalde.workoutlogger.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.view.ActivityDetailWorkout;

public class WorkoutListener implements View.OnClickListener {
    public static final int ACTION_DELETE = 1;
    public static final int ACTION_VIEW_TRACKS = 2;
    public static final String KEY_WORKOUT = "WORKOUT";

    private static final String TAG = WorkoutListener.class.getSimpleName();
    private int action;
    private View view;
    private Workout workout;

    public WorkoutListener (View view, Workout workout, int action) {
        this.action = action;
        this.view = view;
        this.workout = workout;
    }

    @Override
    public void onClick(View view) {
        switch (action) {
            case ACTION_DELETE:
                //todo implement delete
                Toast.makeText(view.getContext(), "Delete not implemented", Toast.LENGTH_LONG).show();
                break;
            case ACTION_VIEW_TRACKS:
                Intent intent = new Intent(view.getContext(), ActivityDetailWorkout.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_WORKOUT, workout);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
                break;
            default:
                Log.e(TAG, "Error: invalid action");
                break;
        }
    }
}
