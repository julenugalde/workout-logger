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
import eus.julenugalde.workoutlogger.controller.CompletedTrainingSessionAdapter;
import eus.julenugalde.workoutlogger.controller.WorkoutAdapter;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityListWorkouts extends AppCompatActivity {
    private ArrayList<Workout> listWorkouts;
    private WorkoutData workoutData;
    private ListView lstWorkouts;
    private FloatingActionButton fabAddWorkout;

    private static final int REQ_CODE_DEF_WORKOUT = 101;
    private static final int REQ_CODE_VIEW_WORKOUT = 102;
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
        //Button new Workout
        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityListWorkouts.this, ActivityDefineWorkout.class);
                startActivityForResult(intent, REQ_CODE_DEF_WORKOUT);
            }
        });

        //List of workouts
        lstWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ActivityListWorkouts.this, ActivityDetailWorkout.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_WORKOUT, listWorkouts.get(position));
                intent.putExtras(bundle);
                startActivityForResult(intent, REQ_CODE_VIEW_WORKOUT);
            }
        });

        updateWorkoutList();
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
            case REQ_CODE_VIEW_WORKOUT:
                updateWorkoutList();
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

    private void updateWorkoutList() {
        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
            finish();
        }
        try {
            listWorkouts = workoutData.getListWorkouts();
            WorkoutAdapter adapter = new WorkoutAdapter(this, listWorkouts);
            lstWorkouts.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
