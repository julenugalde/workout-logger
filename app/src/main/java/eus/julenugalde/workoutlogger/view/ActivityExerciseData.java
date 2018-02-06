package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.TrainingExercise;
import eus.julenugalde.workoutlogger.model.TrainingSession;

public class ActivityExerciseData extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {
    private CheckBox chkCompleted;
    private TextView[] lblLoads;
    private Spinner[] cmbKgs;
    private TextView[] lblSeparators;
    private Spinner[] cmbGs;
    private TextView[] lblWeightUnits;
    private TrainingExercise trainingExercise;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_data);

        //Retrieve data from ActivityNewTrainingSession
        Bundle bundle = getIntent().getExtras();
        trainingExercise = (TrainingExercise)bundle.
                getSerializable(ActivityNewTrainingSession.KEY_EXERCISE);
        if (trainingExercise == null) {
            Log.e(TAG, "Error receiving training exercise data");
            finish();
        }
        captureControls();
        configControls(savedInstanceState);
        initializeVariables(savedInstanceState);
        updateLoadsVisibility();
    }

    private void initializeVariables(Bundle savedInstanceState) {
        chkCompleted.setChecked(trainingExercise.isCompleted());

        // Load names and weight values are read from the TrainingExercise object
        for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
            if ((!trainingExercise.getLoadName(i).isEmpty()) &&
                    !trainingExercise.getLoadName(i).equals("-")) {
                //Set load name
                lblLoads[i].setText(trainingExercise.getLoadName(i) + ": ");

                //Set integer part of the weight
                String[] array = getResources().getStringArray(R.array.values_kg);
                int j=0;
                int selected = trainingExercise.getLoadKg(i);
                for (j=array.length-1; j>0; j--) {
                    if (Integer.parseInt(array[j]) == selected) break;
                }
                cmbKgs[i].setSelection(j);

                //Set decimal part of the weight
                array = getResources().getStringArray(R.array.values_g);
                selected = trainingExercise.getLoadG(i);
                for (j=array.length-1; j>0; j--) {
                    if (Integer.parseInt(array[j]) == selected) break;
                }
                cmbGs[i].setSelection(j);
            }
        }
    }

    private void configControls(Bundle savedInstanceState) {
        //Set adapters and listeners
        chkCompleted.setOnCheckedChangeListener(this);

        ArrayAdapter<CharSequence> adapterKg = ArrayAdapter.createFromResource(
                this, R.array.values_kg, android.R.layout.simple_spinner_item);
        adapterKg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapterG = ArrayAdapter.createFromResource(
                this, R.array.values_g, android.R.layout.simple_spinner_item);
        adapterG.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
            cmbKgs[i].setAdapter(adapterKg);
            cmbGs[i].setAdapter(adapterG);
        }
    }

    private void captureControls() {
        chkCompleted = (CheckBox)findViewById(R.id.ChkExerciseDataCompleted);

        lblLoads = new TextView[TrainingExercise.MAX_LOADS];
        cmbKgs = new Spinner[TrainingExercise.MAX_LOADS];
        lblSeparators = new TextView[TrainingExercise.MAX_LOADS];
        cmbGs = new Spinner[TrainingExercise.MAX_LOADS];
        lblWeightUnits = new TextView[TrainingExercise.MAX_LOADS];

        Resources r = getResources();
        String name = getPackageName();
        for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
            lblLoads[i] = (TextView)findViewById(r.getIdentifier(
                    "LblExerciseDataLoad" + i, "id", name));
            cmbKgs[i] = (Spinner)findViewById(r.getIdentifier(
                    "CmbExerciseDataKg" + i, "id", name));
            lblSeparators[i] = (TextView)findViewById(r.getIdentifier(
                    "LblExerciseDataDecimalSeparator" + i, "id", name));
            cmbGs[i] = (Spinner)findViewById(r.getIdentifier(
                    "CmbExerciseDataG" + i, "id", name));
            lblWeightUnits[i] = (TextView)findViewById(r.getIdentifier(
                    "LblExerciseDataWeightUnit" + i, "id", name));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        trainingExercise.setCompleted(b);
        updateLoadsVisibility();
    }

    private void updateLoadsVisibility() {
        if (trainingExercise.isCompleted()) {    //Show the controls
            for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
                if ((!trainingExercise.getLoadName(i).isEmpty()) &&
                        !trainingExercise.getLoadName(i).equals("-")) {
                    showControls(i);
                }
                else {
                    hideControls(i);
                }
            }
        } else {  //Hide the controls
            for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
                hideControls(i);
            }
        }
    }

    private void showControls(int loadId) {
        lblLoads[loadId].setVisibility(View.VISIBLE);
        cmbKgs[loadId].setVisibility(View.VISIBLE);
        lblSeparators[loadId].setVisibility(View.VISIBLE);
        cmbGs[loadId].setVisibility(View.VISIBLE);
        lblWeightUnits[loadId].setVisibility(View.VISIBLE);
    }

    private void hideControls(int loadId) {
        lblLoads[loadId].setVisibility(View.INVISIBLE);
        cmbKgs[loadId].setVisibility(View.INVISIBLE);
        lblSeparators[loadId].setVisibility(View.INVISIBLE);
        cmbGs[loadId].setVisibility(View.INVISIBLE);
        lblWeightUnits[loadId].setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_exercise_data_save:
                saveData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exercise_data, menu);
        return true;
    }

    private void saveData() {
        //Update the training exercise data with the values selected
        trainingExercise.setCompleted(chkCompleted.isChecked());
        for (int i=0; i< TrainingExercise.MAX_LOADS; i++) {
            if (!trainingExercise.getLoadName(i).isEmpty()) {
                trainingExercise.setLoad(i,
                        trainingExercise.getLoadName(i),
                        Integer.parseInt((String)cmbKgs[i].getSelectedItem()),
                        Integer.parseInt((String)cmbGs[i].getSelectedItem()));
            }
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(ActivityNewTrainingSession.KEY_EXERCISE, trainingExercise);
        Intent intent = getIntent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}
