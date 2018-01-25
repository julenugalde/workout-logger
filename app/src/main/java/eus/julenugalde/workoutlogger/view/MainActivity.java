package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class MainActivity extends AppCompatActivity {
    private ListView lstTrainingSessionSummary;
    private ArrayList<TrainingSession> listTrainingSessions;
    private String[] arrayStrings;
    private WorkoutData workoutData;

    private static final int REQ_CODE_ADD_TRAINING_SESSION = 101;
    private static final int REQ_CODE_VIEW_WORKOUT = 102;
    private static final int REQ_CODE_VIEW_TRAINING_SESSION = 103;
    private static final int REQ_CODE_SETTINGS = 104;
    private static final int RESULT_ERROR_SAVE = 401;

    private static final String XML_FILE = "workout_logger_db.xml";
    private static final String TAG = "MainActivity";

    protected static final String KEY_TRAINING_SESSION = "TRAINING_SESSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddTrainingSession);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ActivityNewTrainingSession.class);
                startActivityForResult(intent, REQ_CODE_ADD_TRAINING_SESSION);
            }
        });

        lstTrainingSessionSummary = (ListView)findViewById(R.id.LstTrainingSessionSummary);
        lstTrainingSessionSummary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ActivityTrainingSessionDetail.class);
                Bundle bundle = new Bundle();
                int index = listTrainingSessions.size()-position-1; //Showing in inverse order
                bundle.putSerializable(KEY_TRAINING_SESSION, listTrainingSessions.get(index));
                intent.putExtras(bundle);
                startActivityForResult(intent, REQ_CODE_VIEW_TRAINING_SESSION);
                return true;
            }
        });
        workoutData = new WorkoutData(getApplicationContext());
        updateTrainingSessionList();
    }

    private void updateTrainingSessionList() {
        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
            finish();
        }
        try {
            listTrainingSessions = workoutData.getListTrainingSessions();
            arrayStrings = new String[listTrainingSessions.size()];
            Iterator<TrainingSession> iterator = listTrainingSessions.iterator();
            TrainingSession aux;
            int i = listTrainingSessions.size()-1;
            StringBuilder sb = new StringBuilder();
            while(iterator.hasNext()) {
                aux = iterator.next();
                sb.append(DateFormat.format("yyyy-MM-dd", aux.getDate()));
                sb.append("  ");
                sb.append(aux.getNameWorkout());
                arrayStrings[i--] = new String(sb.toString());
                sb.delete(0, sb.length());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_list_item_1, arrayStrings);
            lstTrainingSessionSummary.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        switch(item.getItemId()) {
            case R.id.main_activity_menu_list_workouts:
                //TODO list of workouts
                return true;
            case R.id.main_activity_menu_load_xml:
                //TODO load xml
                return true;
            case R.id.main_activity_menu_save_xml:
                //TODO save xml
                return true;
            case R.id.main_activity_menu_settings:
                //TODO settings windows
                return true;
            case R.id.main_activity_menu_view_graph:
                //TODO view graph
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_ADD_TRAINING_SESSION:
                switch (resultCode) {
                    case RESULT_OK:
                        Toast.makeText(getApplicationContext(),
                                R.string.main_activity_save_session_ok, Toast.LENGTH_LONG).show();
                        updateTrainingSessionList();
                        break;
                    case RESULT_ERROR_SAVE:
                        Toast.makeText(getApplicationContext(),
                                R.string.main_activity_save_session_error, Toast.LENGTH_LONG).show();
                        break;
                }
            case REQ_CODE_VIEW_TRAINING_SESSION:
            case REQ_CODE_VIEW_WORKOUT:
                updateTrainingSessionList();
                break;
            case REQ_CODE_SETTINGS:
                updateLocale();
                break;
            default:
                Log.e(TAG, "Error: invalid requestCode value");
                break;
        }
    }

    private void updateLocale() {
        //Access preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String languageCode = prefs.getString("selectedLocale", "en");

        //Update locale
        Locale myLocale = new Locale(languageCode);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(myLocale);
        getResources().updateConfiguration(configuration, displayMetrics);
    }
}
