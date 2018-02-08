package eus.julenugalde.workoutlogger.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TrackAdapter;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.model.WorkoutDataFactory;
import eus.julenugalde.workoutlogger.model.WorkoutDataSQLite;

/** Activity that shows workout details */
public class ActivityDetailWorkout extends AppCompatActivity {
    private Workout workout;
    private WorkoutData workoutData;
    private static final String TAG = ActivityDetailWorkout.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_workout);
        Bundle bundle = getIntent().getExtras();

        try {
            workout = (Workout) bundle.getSerializable(ActivityListWorkouts.KEY_WORKOUT);
            workoutData = WorkoutDataFactory.getInstance(getApplicationContext());
            setTitle(workout.getName());
            TextView lblNumSessions = (TextView)findViewById(R.id.LblDetailWorkoutNumSessions);
            StringBuilder sb = new StringBuilder();
            Resources resources = getResources();
            if (workout.getNumTracks() == 1) {
                sb.append("1 " + resources.getString(R.string.detail_workout_string_tracks_singular));
            }
            else {
                sb.append(workout.getNumTracks() + " " +
                        resources.getString(R.string.detail_workout_string_tracks_plural));
            }
            if (workout.getNumTrainingSessions() == 1) {
                sb.append("\n1 " +
                        resources.getString(R.string.detail_workout_string_training_sessions_singular));
            }
            else {
                sb.append("\n" + workout.getNumTrainingSessions() + " " +
                        resources.getString(R.string.detail_workout_string_training_sessions_plural));
            }

            lblNumSessions.setText(sb.toString());

            ListView lstTracks = (ListView) findViewById(R.id.LstDetailWorkoutTracks);
            ArrayList<Track> trackArrayList = new ArrayList<Track>();
            Track[] tracks = workout.getTracks();
            for (Track track : tracks) {
                trackArrayList.add(track);
            }
            lstTracks.setAdapter(new TrackAdapter(this, trackArrayList));
        } catch (NullPointerException npex) {
            Log.e(TAG, "Error accessing the workout data from ActivityListWorkouts");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_workout_delete:
                showWorkoutDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
    }

    private void showWorkoutDeleteDialog() {
        //Alert dialog displayed to the user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
        }
        else {  //Database opened
            if (workoutData.getNumTrainingSessions(workout.getName()) == 0) {
                //No recorded training sessions for this workout
                builder.setMessage(R.string.list_workouts_warning_delete_workout_text_without);
            } else {  //There are recorded training sessions
                builder.setMessage(R.string.list_workouts_warning_delete_workout_text_with);
            }
            builder.setTitle(R.string.list_workouts_warning_delete_workout_title).
                    setPositiveButton(R.string.list_workouts_warning_delete_workout_accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(TAG, "Deleting workout " + workout.toString());
                                if (!workoutData.deleteWorkout(workout.getName())) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.detail_workout_delete_error,
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }
                        }).
                    setNegativeButton(R.string.training_session_detail_warning_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.list_workouts_warning_delete_workout_cancelled,
                                        Toast.LENGTH_LONG).show();
                            }
                        }).
            //AlertDialog dialog = builder.create();
                    setIcon(android.R.drawable.ic_dialog_alert).
                    show();
        }
    }
}
