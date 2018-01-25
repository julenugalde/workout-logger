package eus.julenugalde.workoutlogger.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import eus.julenugalde.workoutlogger.R;

public class ActivityNewTrainingSession extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_training_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNewTrainingSession);
        setSupportActionBar(toolbar);


    }

}
