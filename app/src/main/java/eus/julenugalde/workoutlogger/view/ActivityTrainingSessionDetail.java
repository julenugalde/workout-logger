package eus.julenugalde.workoutlogger.view;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityTrainingSessionDetail extends AppCompatActivity {
    private ListView lstTracks;
    private TextView lblDate;
    private TextView lblWorkout;
    private TextView lblComment;
    private WorkoutData workoutData;
    private TrainingSession trainingSession;
    private Workout workout;
    private String[] arrayStrings;

    private static final String TAG = "TrainingSessionDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session_detail);
        Bundle bundle = this.getIntent().getExtras();
        trainingSession = (TrainingSession)bundle.getSerializable(MainActivity.KEY_TRAINING_SESSION);
        Log.d(TAG, "trainingSession: " + trainingSession.toString());
        workoutData = new WorkoutData(getApplicationContext());
        workoutData.open();
        workout = workoutData.getTrainingSession(
                trainingSession.getNameWorkout(), trainingSession.getDate());

        if (workout != null) {
            //Capture and initialize controls
            lstTracks = (ListView)findViewById(R.id.LstTrainingSessionDetailTracksLoads);
            lblDate = (TextView)findViewById(R.id.LblTrainingSessionDetailDate);
            lblWorkout = (TextView)findViewById(R.id.LblTrainingSessionDetailWorkout);
            lblComment = (TextView)findViewById(R.id.LblTrainingSessionDetailComment);

            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.readable_date_format));
            lblDate.setText(sdf.format(trainingSession.getDate()));
            lblWorkout.setText(trainingSession.getNameWorkout());

            String comment = trainingSession.getComment();
            if (comment.equals("")) {
                lblComment.setTypeface(null, Typeface.ITALIC);
                comment = "(" + getString(R.string.training_session_detail_no_comment) + ")";
            }
            lblComment.setText(trainingSession.getComment());
            {
                lblComment.setTypeface(null, Typeface.NORMAL);
            }
            lblComment.setText(comment);

            //ListView adapter
            arrayStrings = new String[workout.getNumTracks()];
            StringBuilder sb;
            Track track;
            Load[] arrayLoads;
            for (int i=0; i<arrayStrings.length; i++) {
                track = workout.getTrack(i);
                sb = new StringBuilder(track.getName());
                sb.append("\n");
                arrayLoads = track.getLoads();
                for (int j=0; j<arrayLoads.length; j++) {
                    if (arrayLoads[j].getName().equals("-")) {
                        sb.append("       (" + getString(R.string.training_session_detail_no_load) + ")");
                    }
                    else {
                        sb.append("       " + arrayLoads[j].getName() + " - " +
                                arrayLoads[j].getKg() + getString(R.string.decimal_separator) +
                                arrayLoads[j].getG() + " kg." );
                        if (j<(arrayLoads.length-1)) {
                            sb.append("\n");
                        }
                    }
                }
                arrayStrings[i] = sb.toString();
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_list_item_1, arrayStrings);
            //TODO DEBUG REMOVE
            //String[] test = {"asdfg", "qweert", "zxcv", "poiuy", "jfgff", "ertyerty", "sfg gsfd sf", "ncbnc", "asdfsf", "adsfadsfa", "eweqwerqew", "dadfsafda"};
            //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, test);
            //////
            lstTracks.setAdapter(arrayAdapter);
        }
        else {
            Toast.makeText(this, getString(R.string.training_session_data_access_error), Toast.LENGTH_LONG).
                    show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //TODO CREATE MENU
        //getMenuInflater().inflate(R.menu.menu_training_session_detail);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //TODO Create menu with delete option
            case 1: //TODO Replace by R.id.menu_xxxx value
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
    }
}
