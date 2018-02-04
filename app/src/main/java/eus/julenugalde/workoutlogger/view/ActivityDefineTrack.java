package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingExercise;

public class ActivityDefineTrack extends AppCompatActivity {
    private Spinner cmbDefaultGroups;
    private EditTextWithCounter txtCustomGroup;
    private EditTextWithCounter[] txtLoads;

    private static final String TAG = ActivityDefineTrack.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_track);
        Bundle bundle = getIntent().getExtras();

        Log.d(TAG, "Track: '" + bundle.getString(ActivityDefineWorkout.KEY_TRACK) +
                "'. Load 0: '" + bundle.getString(ActivityDefineWorkout.KEY_LOADS[0]) +
                "'. Load 1: '" + bundle.getString(ActivityDefineWorkout.KEY_LOADS[1]) +
                "'. Load 2: '" + bundle.getString(ActivityDefineWorkout.KEY_LOADS[2]) +
                "'. Load 3: '" + bundle.getString(ActivityDefineWorkout.KEY_LOADS[3]) + "'");

        captureControls();
        initializeControls();
        setListeners();
    }

    private void captureControls() {
        cmbDefaultGroups = (Spinner)findViewById(R.id.CmbDefineTrackDefaultGroups);
        txtCustomGroup = (EditTextWithCounter)findViewById(R.id.TxtDefineTrackCustomGroup);
        txtLoads = new EditTextWithCounter[TrainingExercise.MAX_LOADS];
        for (int i=0; i<txtLoads.length; i++) {
            txtLoads[i] = (EditTextWithCounter)findViewById(getResources().getIdentifier(
                    "TxtDefineTrackLoad" + i, "id", getPackageName()));
        }
    }

    private void initializeControls() {
        //Character limits for text fields
        txtCustomGroup.setCountLimit(Track.NAME_MAX_LENGTH);
        for (int i=0; i<ActivityDefineWorkout.KEY_LOADS.length; i++) {
            txtLoads[i].setCountLimit(Load.NAME_MAX_LENGTH);
        }

        //Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.defaultTracks, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbDefaultGroups.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        String readTrack = bundle.getString(ActivityDefineWorkout.KEY_TRACK);
        if (readTrack != null) {    //We're editing the track
            //Set the selection spinner selection and don't allow changes
            int index = 0;
            String[] defaultTracks = getResources().getStringArray(R.array.defaultTracks);
            while((readTrack.compareTo(defaultTracks[index]) != 0) &&
                    (index < (defaultTracks.length-1))) {
                index++;
            }
            cmbDefaultGroups.setEnabled(false);

            //Set the custom track name field
            cmbDefaultGroups.setSelection(index);
            if (index == (defaultTracks.length-1)) {    //Custom track
                txtCustomGroup.setVisibility(View.VISIBLE);
                txtCustomGroup.setText(readTrack);
            }
            else {  //Track in the default tracks list
                txtCustomGroup.setVisibility(View.INVISIBLE);
                txtCustomGroup.setText("");
            }

            //Set the load names
            String readLoad;
            for (int i=0; i<ActivityDefineWorkout.KEY_LOADS.length; i++) {
                readLoad = bundle.getString(ActivityDefineWorkout.KEY_LOADS[i]);
                if (readLoad != null) {
                    txtLoads[i].setText(readLoad);
                }
            }

        }
        else {  //It's a new track
            cmbDefaultGroups.setEnabled(true);
            int defaultPosition = bundle.getInt(ActivityDefineWorkout.KEY_POSITION);
            if (defaultPosition < getResources().getStringArray(R.array.defaultTracks).length) {
                cmbDefaultGroups.setSelection(defaultPosition);
            }
        }
    }

    private void setListeners() {
        cmbDefaultGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int lastPosition = (getResources().getStringArray(R.array.defaultTracks).length) - 1;
                if (position == lastPosition) { //Custom track
                    txtCustomGroup.setVisibility(View.VISIBLE);
                    txtCustomGroup.setEnabled(true);
                    txtCustomGroup.requestFocus();
                }
                else {  //Selection within the default tracks list
                    txtCustomGroup.setVisibility(View.INVISIBLE);
                    txtCustomGroup.setEnabled(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_define_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_define_track_save:
                saveTrack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Save the track data in the bundle and return to the workout define activity */
    private void saveTrack() {
        Bundle bundle = getIntent().getExtras();
        String trackName = (String)cmbDefaultGroups.getSelectedItem();
        if (cmbDefaultGroups.getSelectedItemPosition() ==
                (getResources().getStringArray(R.array.defaultTracks).length-1)) {
            trackName = txtCustomGroup.getText().toString();
        }
        if (!trackName.isEmpty()) {
            bundle.putString(ActivityDefineWorkout.KEY_TRACK, trackName);
            for (int i=0; i<txtLoads.length; i++) {
                //The loads are only saved if the text is not empty
                bundle.putString(ActivityDefineWorkout.KEY_LOADS[i], txtLoads[i].getText().toString());
            }
            Intent intent = getIntent().putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.define_track_error_empty,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
