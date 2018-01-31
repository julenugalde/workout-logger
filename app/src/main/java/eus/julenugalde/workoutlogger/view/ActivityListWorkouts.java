package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityListWorkouts extends AppCompatActivity {
    private ArrayList<Workout> listWorkouts;
    private HashMap<Workout, Integer> exercisesHashMap;
    private WorkoutData workoutData;
    private ListView lstWorkouts;
    private FloatingActionButton fabAddWorkout;

    private static final int REQ_CODE_DEF_WORKOUT = 101;
    private static final String TAG = ActivityListWorkouts.class.getSimpleName();

    public static final String KEY_WORKOUT = "WORKOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workouts);
        workoutData = new WorkoutData(getApplicationContext());

        captureControls();
        initializeControls();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeControls() {
        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityListWorkouts.this, ActivityDefineWokout.class);
                startActivityForResult(intent, REQ_CODE_DEF_WORKOUT);
            }
        });

        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
            finish();   //TODO Change open() method calls in the project so they test if DB is correctly opened
        }
        listWorkouts = workoutData.getListWorkouts();

        exercisesHashMap = new HashMap<Workout, Integer>();
        Workout aux;
        for (int i=0; i<listWorkouts.size(); i++) {
            aux = listWorkouts.get(i);
            exercisesHashMap.put(aux, workoutData.getNumTrainingSessions(aux.getName()));
        }
        workoutData.close();
    }

    private void captureControls() {
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        lstWorkouts = (ListView)findViewById(R.id.LstWorkouts);
        fabAddWorkout = (FloatingActionButton) findViewById(R.id.fabAddWorkout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQ_CODE_DEF_WORKOUT:
                //TODO implement
                break;
            default:
                Log.e(TAG, "Invalid reqCode: " + requestCode);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_OK);
        workoutData.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_ctx_workout, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = adapterContextMenuInfo.position;
        final Workout selectedWorkout = listWorkouts.get(position);
        Log.d(TAG, "Selected workout: " + selectedWorkout.toString());
        Log.d(TAG, workoutData.getNumTrainingSessions(selectedWorkout.getName()) +
                " training sessions for the workout");

        switch(item.getItemId()) {
            case R.id.CtxLblDeleteWorkout:
                //TODO IMPLEMENT DELETE
                return true;
            case R.id.CtxLblDetailWorkout:
                //TODO IMPLEMENT DETAIL
                return true;
            default:
                Log.e(TAG, "Invalid item id: " + item.getItemId());
                return super.onContextItemSelected(item);
        }
    }
}
