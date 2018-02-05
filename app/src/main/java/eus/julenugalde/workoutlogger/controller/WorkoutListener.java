package eus.julenugalde.workoutlogger.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.view.ActivityDetailWorkout;

public class WorkoutListener implements View.OnClickListener {
    public static final int ACTION_DELETE = 1;
    public static final int ACTION_VIEW_TRACKS = 2;
    public static final String KEY_WORKOUT = "WORKOUT";

    private static final String TAG = WorkoutListener.class.getSimpleName();
    private int action;
    private View view;
    private Workout workout;
    private WorkoutData workoutData;

    public WorkoutListener (View view, Workout workout, WorkoutData workoutData, int action) {
        this.action = action;
        this.view = view;
        this.workout = workout;
        this.workoutData = workoutData;
    }

    @Override
    public void onClick(final View view) {
        switch (action) {
            case ACTION_DELETE:
                Log.d(TAG, "Deleting workout " + workout.toString());
                //Alert dialog displayed to the user
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                if (workoutData.getNumTrainingSessions(workout.getName()) == 0) {
                    //No recorded training sessions for this workout
                    builder.setMessage(R.string.list_workouts_warning_delete_workout_text_without);
                }
                else {  //There are recorded training sessions
                    builder.setMessage(R.string.list_workouts_warning_delete_workout_text_with);
                }
                builder.setTitle(R.string.list_workouts_warning_delete_workout_title);
                builder.setPositiveButton(R.string.list_workouts_warning_delete_workout_accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                workoutData.deleteWorkout(workout.getName());
                                //TODO Update view
                                /*listWorkouts.remove(workout);
                                view.requestLayout();*/
                            }
                        });
                builder.setNegativeButton(R.string.training_session_detail_warning_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(view.getContext(),
                                        R.string.list_workouts_warning_delete_workout_cancelled,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
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
