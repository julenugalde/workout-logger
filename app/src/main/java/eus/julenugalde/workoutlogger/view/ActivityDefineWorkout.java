package eus.julenugalde.workoutlogger.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TextWithCounterWatcher;
import eus.julenugalde.workoutlogger.controller.TrackAdapter;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.model.WorkoutDataFactory;
import eus.julenugalde.workoutlogger.model.WorkoutDataSQLite;

/** Activity for defining a new workout */
public class ActivityDefineWorkout extends AppCompatActivity {
    public static final String KEY_POSITION = "POSITION";
    public static final String KEY_TRACK = "TRACK";
    public static final String[] KEY_LOADS = {"LOAD0", "LOAD1", "LOAD2", "LOAD3"};

    private static final int REQ_CODE_NEW_TRACK = 101;
    private static final int REQ_CODE_EDIT_TRACK = 102;
    private static final String TAG = ActivityDefineWorkout.class.getSimpleName();

    private ArrayList<Track> trackArrayList;
    private EditTextWithCounter txtWorkoutName;
    private Button btnNewTrack;
    private ListView lstTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_workout);
        trackArrayList = new ArrayList<Track>();

        captureControls();
        initializeControls();
    }

    private void initializeControls() {
        txtWorkoutName.setCountLimit(Workout.NAME_MAX_LENGTH);
        txtWorkoutName.setText(suggestWorkoutName());
        txtWorkoutName.addTextChangedListener(new TextWithCounterWatcher(txtWorkoutName));

        lstTracks.setAdapter(new TrackAdapter(this, trackArrayList));

        btnNewTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityDefineWorkout.this, ActivityDefineTrack.class);
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_POSITION, trackArrayList.size());
                intent.putExtras(bundle);
                startActivityForResult(intent, REQ_CODE_NEW_TRACK);
            }
        });
    }

    private void captureControls() {
        txtWorkoutName = (EditTextWithCounter)findViewById(R.id.TxtDefineWorkoutName);
        btnNewTrack = (Button)findViewById(R.id.BtnDefineWorkoutNewTrack);
        lstTracks = (ListView)findViewById(R.id.LstDefineWorkoutsTracks);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode) {
            case Activity.RESULT_OK:
                updateListTracks(requestCode, data);
                break;
            case Activity.RESULT_CANCELED:
                Log.d(TAG, "Operation cancelled by the user");
        }
    }

    private void updateListTracks(int requestCode, Intent data) {
        try {
            Track track;
            switch (requestCode) {
                case REQ_CODE_NEW_TRACK:    //Return from an ActivityDefineTrack
                    track = readBundleData(data);
                    if(existsTrack(track)) {
                        Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_exists,
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        trackArrayList.add(track);
                        lstTracks.requestLayout();
                    }
                    break;
                case REQ_CODE_EDIT_TRACK:   //The track information has been updated

                    break;
            }
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getLocalizedMessage());
        }
    }

    private boolean existsTrack(Track track) {
        Iterator<Track> iterator = trackArrayList.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().getName().equalsIgnoreCase(track.getName())) {
                return true;
            }
        }
        return false;
    }

    private Track readBundleData(Intent data) {
        Track track = new Track();
        Bundle bundle = data.getExtras();
        Log.d(TAG, "Received track: " + bundle.getString(KEY_TRACK));

        track.setName(bundle.getString(KEY_TRACK));
        String aux;
        for (int i=0; i<KEY_LOADS.length; i++) {
            aux = bundle.getString(KEY_LOADS[i]);
            if (aux != null) {
                track.addLoad(new Load(aux));
            }
        }
        return track;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_define_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_define_workout_save:
                saveWorkout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveWorkout() {
        String name = txtWorkoutName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_empty_name,
                    Toast.LENGTH_LONG).show();
        }
        else if (trackArrayList.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_no_tracks,
                    Toast.LENGTH_LONG).show();
        }
        else {
            //Fill the workout fields
            Workout workout = new Workout(name);
            Iterator<Track> iterator = trackArrayList.iterator();
            while (iterator.hasNext()) {
                workout.addTrack(iterator.next());
            }

            //Store the workout in the database
            WorkoutData workoutData = WorkoutDataFactory.getInstance(getApplicationContext());
            if(workoutData.open()) {
                if (workoutData.existsWorkout(workout.getName())) {
                    Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_exists,
                            Toast.LENGTH_LONG).show();
                }
                else {
                    if (workoutData.insertWorkout(workout)) {
                        setResult(RESULT_OK);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_db,
                                Toast.LENGTH_LONG).show();
                    }
                }
                workoutData.close();
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.define_workout_save_error_db,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /** For the case of BodyPump workouts, the next workout is suggested */
    private String suggestWorkoutName() {
        WorkoutData workoutData = WorkoutDataFactory.getInstance(getApplicationContext());
        if(!workoutData.open()) {
            Log.e(TAG, "Error opening the database");
            return "";
        }
        boolean flagBodyPump = false;
        int lastRelease=0;
        int i;
        String s;
        StringTokenizer st;
        ArrayList<Workout> workoutArrayList = workoutData.getListWorkouts();
        Iterator<Workout> iterator = workoutArrayList.iterator();
        while (iterator.hasNext()) {
            st = new StringTokenizer(iterator.next().getName(), " ");
            flagBodyPump = false;
            i = 0;
            s = "";
            while (st.hasMoreTokens()) {
                s = st.nextToken();
                if (s.equalsIgnoreCase("BodyPump")) {
                    flagBodyPump = true;
                }
            }
            if (flagBodyPump) {
                try {
                    i = Integer.parseInt(s); //The last token is the release number.
                } catch (NumberFormatException e) {}
                lastRelease = (i>lastRelease) ? i : lastRelease;
            }
        }

        workoutData.close();
        return "BodyPump " + (++lastRelease);
    }
}
