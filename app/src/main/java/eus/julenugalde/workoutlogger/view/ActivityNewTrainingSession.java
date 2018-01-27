package eus.julenugalde.workoutlogger.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TrainingExerciseAdapter;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingExercise;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityNewTrainingSession extends AppCompatActivity implements OnItemSelectedListener, OnItemClickListener {
    private EditText txtDate;
    private Spinner cmbListWorkouts;
    private ImageButton btnDate;
    private ListView lstTrainingExercises;
    private EditTextWithCounter txtComment;

    private WorkoutData workoutData;
    private ArrayList<TrainingExercise> listExercises;
    private String[] arrayWorkouts;
    private int positionCombo;

    private static final int REQ_CODE_DATA_EXERCISE = 101;
    private static final int REQ_CODE_DATA_TRAINING_SESSION = 102;
    private static final int RESULT_ERROR_SAVE = 401;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_training_session);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNewTrainingSession);
        //setSupportActionBar(toolbar);
        workoutData = new WorkoutData(this);

        findControls();
        initializeVariables(savedInstanceState);
        configControls(savedInstanceState);

    }

    private void configControls(Bundle savedInstanceState) {
        //Spinner with list of workouts
        ArrayAdapter<String> adapterListWorkouts = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, arrayWorkouts);
        adapterListWorkouts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbListWorkouts.setAdapter(adapterListWorkouts);
        cmbListWorkouts.setSelection(positionCombo);
        cmbListWorkouts.setOnItemSelectedListener(this);
        if (savedInstanceState == null) { //Exercise list is not loaded
            loadSelectedWorkoutData(false);
        }

        //ListView with list of training exercises
        TrainingExerciseAdapter trainingExerciseAdapter =
                new TrainingExerciseAdapter(this, listExercises);
        lstTrainingExercises.setAdapter(trainingExerciseAdapter);
        lstTrainingExercises.setOnItemClickListener(this);

        //EditText with date
        txtDate.setText(DateFormat.format("yyyy-MM-dd", new Date()));
        txtDate.setEnabled(false);
        txtDate.setTextColor(getResources().getColor(R.color.colorPrimaryText));
        btnDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Open a dialog to pick a date
                Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_LONG).show();
            }
        });

        //TextView for the comment
        txtComment.setText("");
        txtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Make sure that the text length limit is not surpassed
                if (charSequence.length() > TrainingSession.MAX_LENGTH_COMMENT) {
                    txtComment.setText(
                            charSequence.subSequence(0, TrainingSession.MAX_LENGTH_COMMENT-1));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void loadSelectedWorkoutData(boolean clean) {   //TODO decide if this should return a boolean
        workoutData.open();
        Workout currentWorkout = workoutData.getWorkout(cmbListWorkouts.getSelectedItem().toString());
        workoutData.close();
        if (currentWorkout == null) {
            Log.e(TAG, "Error loading selected workout");
        }
        else {
            if (clean) listExercises.clear();
            TrainingExercise trainingExercise;
            Track[] arrayTracks = currentWorkout.getTracks();
            Load[] arrayLoads;
            for (int i=0; i<currentWorkout.getNumTracks(); i++) {
                //By default we consider that the exercise was not completed
                trainingExercise = new TrainingExercise(arrayTracks[i].getName(), false);
                if ((arrayLoads = arrayTracks[i].getLoads()) != null) { //Track contains loads
                    //TODO check if this is correct
                    for (int j=0; j<arrayLoads.length; j++) {
                        trainingExercise.setLoad(j, arrayLoads[j].getName(),
                                arrayLoads[j].getKg(), arrayLoads[j].getG());
                    }
                    //////////////////
                }
                listExercises.add(trainingExercise);
            }
        }
    }

    private void initializeVariables(Bundle savedInstanceState) {
        if(savedInstanceState != null) {    //Data have been saved
            try {
                positionCombo = savedInstanceState.getInt("COMBO_POSITION");
                listExercises = (ArrayList<TrainingExercise>)(savedInstanceState.
                        getSerializable("LIST_EXERCISES"));
                arrayWorkouts = savedInstanceState.getStringArray("ARRAY_WORKOUTS");
            }catch (ClassCastException ccex) {
                Log.e(TAG, "Error in the cast of the exercises list: " + ccex.getLocalizedMessage());
            }
        }
        else {
            positionCombo = 0;
            workoutData.open();
            ArrayList<Workout> listWorkouts = workoutData.getListWorkouts();
            workoutData.close();
            arrayWorkouts = new String[listWorkouts.size()];
            int i = listWorkouts.size()-1;  //Display in inverse order
            Iterator<Workout> iterator = listWorkouts.iterator();
            while(iterator.hasNext()) {
                arrayWorkouts[i--] = iterator.next().getName();
            }
            listExercises = new ArrayList<TrainingExercise>();
        }
    }

    private void findControls() {
        txtDate = (EditText)findViewById(R.id.TxtNewTrainingSessionDate);
        cmbListWorkouts = (Spinner)findViewById(R.id.CmbListWorkouts);
        btnDate = (ImageButton)findViewById(R.id.BtnNewTrainingSessionDate);
        lstTrainingExercises = (ListView)findViewById(R.id.LstNewTrainingSessionExercises);
        txtComment = (EditTextWithCounter)findViewById(R.id.TxtNewtrainingSessionComment);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO Create the menu layout and inflate here
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO Define the action for the menu items
        return true;
    }
}
