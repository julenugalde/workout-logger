package eus.julenugalde.workoutlogger.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TrackAdapter;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.Workout;

/** Activity that shows workout details */
public class ActivityDetailWorkout extends AppCompatActivity {
    private static final String TAG = ActivityDetailWorkout.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_workout);
        Bundle bundle = getIntent().getExtras();

        try {
            Workout workout = (Workout) bundle.getSerializable(ActivityListWorkouts.KEY_WORKOUT);
            setTitle(workout.getName());

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
}
