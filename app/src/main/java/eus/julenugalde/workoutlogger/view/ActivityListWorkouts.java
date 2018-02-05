package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.WorkoutAdapter;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityListWorkouts extends AppCompatActivity {
    private ArrayList<Workout> listWorkouts;
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
                Intent intent = new Intent(ActivityListWorkouts.this, ActivityDefineWorkout.class);
                startActivityForResult(intent, REQ_CODE_DEF_WORKOUT);
            }
        });

        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
            finish();   //TODO Change open() method calls in the project so they test if DB is correctly opened
        }
        listWorkouts = workoutData.getListWorkouts();
        Log.d(TAG, listWorkouts.size() + " workouts in the database");
        lstWorkouts.setAdapter(new WorkoutAdapter(this, listWorkouts));
    }

    private void captureControls() {
        lstWorkouts = (ListView)findViewById(R.id.LstWorkouts);
        fabAddWorkout = (FloatingActionButton) findViewById(R.id.fabAddWorkout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQ_CODE_DEF_WORKOUT:
                listWorkouts = workoutData.getListWorkouts();
                lstWorkouts.setAdapter(new WorkoutAdapter(this, listWorkouts));
                ((WorkoutAdapter)lstWorkouts.getAdapter()).notifyDataSetChanged();
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
}
