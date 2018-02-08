package eus.julenugalde.workoutlogger.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TrainingExerciseAdapter;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingExercise;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.model.WorkoutDataFactory;
import eus.julenugalde.workoutlogger.model.WorkoutDataSQLite;

/** This activity is displayed when a training session is selected from the list in the main
 * activity. It shows the information of the {@link TrainingSession} object and all the details
 * of the associated {@link TrainingExercise} objects. It allows to delete the training session.
 */
public class ActivityTrainingSessionDetail extends AppCompatActivity {
    private ListView lstTracks;
    private TextView lblDate;
    private TextView lblWorkout;
    private TextView lblComment;
    private WorkoutData workoutData;
    private TrainingSession trainingSession;
    private Workout workout;

    private static final String TAG = ActivityTrainingSessionDetail.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session_detail);
        Bundle bundle = this.getIntent().getExtras();
        trainingSession = (TrainingSession)bundle.getSerializable(MainActivity.KEY_TRAINING_SESSION);
        if (trainingSession == null) {
            Log.e(TAG, "Error retrieving training session data");
            finish();
        }
        workoutData = WorkoutDataFactory.getInstance(getApplicationContext());
        if (workoutData.open()) {
            workout = workoutData.getTrainingSession(
                    trainingSession.getNameWorkout(), trainingSession.getDate());
        }
        else {
            workout = null;
        }

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
            else {
                lblComment.setTypeface(null, Typeface.NORMAL);
            }
            lblComment.setText(comment);

            //ListView adapter
            ArrayList<TrainingExercise> arrayTrainingExercise = new ArrayList<TrainingExercise>();
            TrainingExercise trainingExercise;
            Track track;
            Load[] arrayLoads;
            for (int i=0; i<workout.getNumTracks(); i++) {
                track = workout.getTrack(i);
                trainingExercise = new TrainingExercise(track.getName(), true);
                arrayLoads = track.getLoads();
                for (int j=0; j<arrayLoads.length; j++) {
                    track.addLoad(arrayLoads[j]);
                    trainingExercise.setLoad(j, arrayLoads[j].getName(), arrayLoads[j].getKg(),
                            arrayLoads[j].getG());
                }
                arrayTrainingExercise.add(trainingExercise);
            }
            TrainingExerciseAdapter trainingExerciseAdapter =
                    new TrainingExerciseAdapter(this, arrayTrainingExercise);
            lstTracks.setAdapter(trainingExerciseAdapter);
        }
        else {
            Toast.makeText(
                    this, getString(R.string.training_session_data_access_error), Toast.LENGTH_LONG).
                    show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_training_session_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_training_session_detail_delete:
                showTrainingSessionDeleteDialog();
                return true;
            case R.id.menu_training_session_detail_edit:
                editTrainingSession();
                return true;
            default:
                return false;
        }
    }

    private void editTrainingSession() {
        //TODO Implement editing training session data
        Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }

    private void showTrainingSessionDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.training_session_detail_warning_delete);
        builder.setPositiveButton(R.string.training_session_detail_warning_accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (workoutData.open()) {
                            Log.i(TAG, "Deleting training session " + trainingSession.toString());
                            if(!workoutData.deleteTrainingSession(trainingSession)) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.training_session_detail_delete_error,
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                setResult(RESULT_OK);
                                finish();
                            }
                            workoutData.close();
                        }
                        else  {
                            Toast.makeText(getApplicationContext(),
                                    R.string.open_db_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        builder.setNegativeButton(R.string.training_session_detail_warning_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "Delete action cancelled");
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
    }
}
