package eus.julenugalde.workoutlogger.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.LocalesManager;
import eus.julenugalde.workoutlogger.controller.TextWithCounterWatcher;
import eus.julenugalde.workoutlogger.controller.TrainingExerciseAdapter;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingExercise;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.model.WorkoutDataFactory;

/** Activity to create a new training session or edit an existing one */
public class ActivityNewTrainingSession extends AppCompatActivity
        implements OnItemSelectedListener, OnItemClickListener, DatePickerDialog.OnDateSetListener {
    public final static String KEY_EXERCISE = "EXERCISE";
    public final static String KEY_DATE = "DATE";
    public final static String KEY_COMBO_POSITION = "COMBO_POSITION";
    public final static String KEY_LIST_EXERCISES = "LIST_EXERCISES";
    public final static String KEY_ARRAY_WORKOUTS = "ARRAY_WORKOUTS";

    private EditText txtDate;
    private Spinner cmbListWorkouts;
    private ImageButton btnDate;
    private ListView lstTrainingExercises;
    private EditTextWithCounter txtComment;
    private DatePickerDialog datePickerDialog;

    private WorkoutData workoutData;
    private ArrayList<TrainingExercise> listExercises;
    private String[] arrayWorkouts;
    private int positionCombo;
    private Calendar date;
    private boolean editing = false;
    private TrainingSession retrievedTrainingSession;

    private static final int REQ_CODE_EXERCISE_DATA = 101;
    private static final int REQ_CODE_TRAINING_SESSION_DATE = 102;
    private static final int RESULT_ERROR_SAVE = 401;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_training_session);
        workoutData = WorkoutDataFactory.getInstance(getApplicationContext());

        captureControls();
        initializeVariables(savedInstanceState);
        configControls(savedInstanceState);
        loadPreviousData();

        txtComment.requestFocus();
    }

    /** Checks if it's a new training session or we're editing a previous one. In the latter case,
     * the previous data are loaded     */
    private void loadPreviousData() {
        Bundle bundle = getIntent().getExtras();
        try {   //to get training session data from parent activity
            Workout workout =
                    (Workout) bundle.getSerializable(ActivityTrainingSessionDetail.KEY_WORKOUT);
            retrievedTrainingSession =
                    (TrainingSession)bundle.get(ActivityTrainingSessionDetail.KEY_TRAINING_SESSION);

            //Complete fields with data from parent activity
            txtDate.setText(DateFormat.format("yyyy-MM-dd", retrievedTrainingSession.getDate()));
            txtComment.setText(retrievedTrainingSession.getComment());

            //For each recorded track a TrainingExercise will be added to the list and set as completed
            TrainingExercise trainingExercise;
            Track[] recordedTracks = workout.getTracks();
            Load[] loads;
            int index;
            for (Track track : recordedTracks) {
                Log.d(TAG, track.toString());   //TODO duplicated loads. fix
                trainingExercise = new TrainingExercise(track.getName(), true);
                for (int j=0; j<recordedTracks.length; j++) {
                    loads = workout.getTrack(j).getLoads();
                    for (int i = 0; i < loads.length; i++) {
                        trainingExercise.setLoad(i, loads[i].getName(), loads[i].getKg(), loads[i].getG());
                    }
                }
                index = getTrainingExerciseIndex(track.getName());
                if (index == -1) {  //Not in the list (e.g. custom track)
                    listExercises.add(trainingExercise);
                }
                else {  //Replace in the existing list
                    listExercises.set(index, trainingExercise);
                }
            }
            //No NullpointerException ocurred --> we're editing
            setTitle(R.string.new_training_session_edit_title);
            editing = true;
            lstTrainingExercises.requestLayout();
        } catch (NullPointerException npex) {
            // New training session. Do nothing
            editing = false;    //Not really needed
        }
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

        //DatePickerDialog
        date = GregorianCalendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, this,
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));

        //EditText with date
        txtDate.setText(DateFormat.format("yyyy-MM-dd", date.getTime()));
        txtDate.setEnabled(false);
        btnDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        //TextView for the comment
        txtComment.setCountLimit(TrainingSession.MAX_LENGTH_COMMENT);
        txtComment.setText("");
        txtComment.addTextChangedListener(new TextWithCounterWatcher(txtComment));

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        txtDate.setText(DateFormat.format("yyyy-MM-dd", date.getTime()));
    }

    private boolean loadSelectedWorkoutData(boolean clean) {
        if (!workoutData.open()) {
            Log.e(TAG, "Error opening the database");
            return false;
        }
        Workout currentWorkout = workoutData.getWorkout(cmbListWorkouts.getSelectedItem().toString());
        workoutData.close();
        if (currentWorkout == null) {
            Log.e(TAG, "Error loading selected workout");
            return false;
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
                    for (int j=0; j<arrayLoads.length; j++) {
                        trainingExercise.setLoad(j, arrayLoads[j].getName(),
                                arrayLoads[j].getKg(), arrayLoads[j].getG());
                    }
                }
                listExercises.add(trainingExercise);
            }
        }
        return true;
    }

    private void initializeVariables(Bundle savedInstanceState) {
        if(savedInstanceState != null) {    //Data have been saved
            try {
                positionCombo = savedInstanceState.getInt(KEY_COMBO_POSITION, 0);
                listExercises = (ArrayList<TrainingExercise>)savedInstanceState.
                        getSerializable(KEY_LIST_EXERCISES);
                if (listExercises == null) {
                    listExercises = new ArrayList<>();
                }
                arrayWorkouts = savedInstanceState.getStringArray(KEY_ARRAY_WORKOUTS);
                if (arrayWorkouts == null) {
                    arrayWorkouts = new String[] {};
                }
            }catch (ClassCastException ccex) {
                Log.e(TAG, "Error in the cast of exercises list: " + ccex.getLocalizedMessage());
            }
        }
        else {
            positionCombo = 0;
            if (workoutData.open()) {
                ArrayList<Workout> listWorkouts = workoutData.getListWorkouts();
                workoutData.close();
                arrayWorkouts = new String[listWorkouts.size()];
                int i = listWorkouts.size() - 1;  //Display in inverse order
                Iterator<Workout> iterator = listWorkouts.iterator();
                while (iterator.hasNext()) {
                    arrayWorkouts[i--] = iterator.next().getName();
                }
                workoutData.close();
            }
            else {  //Error opening the database
                arrayWorkouts = new String[] {getResources().getString(R.string.open_db_error)};
                Log.e(TAG, "Error opening the database");
            }
            listExercises = new ArrayList<TrainingExercise>();
        }
    }

    private void captureControls() {
        txtDate = (EditText)findViewById(R.id.TxtNewTrainingSessionDate);
        cmbListWorkouts = (Spinner)findViewById(R.id.CmbListWorkouts);
        btnDate = (ImageButton)findViewById(R.id.BtnNewTrainingSessionDate);
        lstTrainingExercises = (ListView)findViewById(R.id.LstNewTrainingSessionExercises);
        txtComment = (EditTextWithCounter)findViewById(R.id.TxtNewtrainingSessionComment);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        TrainingExercise currentExercise = listExercises.get(position);
        Intent intent = new Intent(ActivityNewTrainingSession.this, ActivityExerciseData.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_EXERCISE, currentExercise);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQ_CODE_EXERCISE_DATA);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (positionCombo != position) {
            positionCombo = position;
            loadSelectedWorkoutData(true);
            BaseAdapter baseAdapter = ((BaseAdapter)lstTrainingExercises.getAdapter());
            if (baseAdapter != null) {
                baseAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        cmbListWorkouts.setSelection(0);
        loadSelectedWorkoutData(true);
        lstTrainingExercises.requestLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_training_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_new_training_session_save:
                boolean anyExercise = false;    //Is there any exercise in the training session?
                for (TrainingExercise trainingExercise : listExercises) {
                    if(trainingExercise.isCompleted()) anyExercise = true;
                }
                if (anyExercise) { //There are exercises --> Save data and return to main activity
                    if (!workoutData.open()) {
                        Log.e(TAG, "Error opening the database");
                        return false;
                    }
                    saveTrainingSession();
                    workoutData.close();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.new_training_session_empty,
                            Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Save the training session information in the database and set the activity result. If
     * we're editing, a new training session will be added to the database and the existing one
     * will be deleted. */
    private void saveTrainingSession() {
        //Workout list is in inverse order
        ArrayList<Workout> listWorkouts = workoutData.getListWorkouts();
        int index = listWorkouts.size() - cmbListWorkouts.getSelectedItemPosition() - 1;
        if (workoutData.insertTrainingSession(
                listWorkouts.get(index),            //Workout
                listExercises,                      //List TrainingExercises
                txtDate.getText().toString(),       //Date
                txtComment.getText().toString())) {  //Comment
            setResult(RESULT_OK);
            if (editing) {  //Remove the previous one
                if (!workoutData.deleteTrainingSession(retrievedTrainingSession)) {
                    setResult(RESULT_ERROR_SAVE);
                }
            }
        }
        else {      //Error inserting in the database
            setResult(RESULT_ERROR_SAVE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQ_CODE_EXERCISE_DATA:    //Training exercise data to update in the list
                if ((resultCode == RESULT_OK) && (data != null)) {
                    Bundle bundle = data.getExtras();
                    TrainingExercise result = (TrainingExercise)bundle.getSerializable(KEY_EXERCISE);
                    //Replace the training session data in the list
                    if (result != null) {
                        TrainingExercise aux;
                        for (int i = 0; i < listExercises.size(); i++) {//Replace if already existing
                            aux = listExercises.get(i);
                            if (aux.getName().compareTo(result.getName()) == 0) {
                                listExercises.remove(i);
                                listExercises.add(i, result);
                                break;
                            }
                        }
                        ((BaseAdapter) lstTrainingExercises.getAdapter()).notifyDataSetChanged();
                        lstTrainingExercises.requestLayout();
                    }
                }
                break;

            case REQ_CODE_TRAINING_SESSION_DATE:    //Update date information
                if ((resultCode == RESULT_OK) && (data != null)) {
                    Bundle bundle = data.getExtras();
                    int[] array = bundle.getIntArray(KEY_DATE);
                    if (array.length < 3) {
                        Log.e(TAG, "Error retrieving date info");
                    }
                    else {
                        GregorianCalendar gregorianCalendar = new GregorianCalendar(
                                array[0], array[1], array[2]);
                        txtDate.setText(DateFormat.format("yyyy-MM-dd", gregorianCalendar.getTime()));
                    }
                }
                break;
            default:
                Log.e(TAG, "requestCode not valid");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_LIST_EXERCISES, listExercises);
        outState.putStringArray(KEY_ARRAY_WORKOUTS, arrayWorkouts);
        outState.putInt(KEY_COMBO_POSITION, positionCombo);
    }

    /** returns the index of a TrainingExercise in the ArrayList listExercises. Will return -1 if
     * not found */
    private int getTrainingExerciseIndex(String trackName) {
        //The default track names are defined in multiple languages (locales). It has to be
        //taken into account that the training session might have been defined in a particular
        //language and in the editing we're using a different one.
        String[] locales = LocalesManager.getLocales();
        String[][] defaultTrackNames = LocalesManager.getDefaultTrackNames();
        for (int i=0; i<locales.length; i++) {
            for (int j=0; j<defaultTrackNames[i].length; j++) {
                if (trackName.equalsIgnoreCase(defaultTrackNames[i][j])) {
                    Log.d(TAG, trackName + "=" + defaultTrackNames[i][j] + " (" + i + j + ")");
                    return j;
                }
            }
        }
        return -1;
    }
}
