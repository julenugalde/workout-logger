package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.autofill.FillEventHistory;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.CompletedTrainingSessionAdapter;
import eus.julenugalde.workoutlogger.model.Persistence;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.WorkoutData;
import eus.julenugalde.workoutlogger.model.XMLPersistence;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ListView lstTrainingSessionSummary;
    private ArrayList<TrainingSession> listTrainingSessions;
    private WorkoutData workoutData;
    private Persistence persistence;

    private static final int REQ_CODE_ADD_TRAINING_SESSION = 101;
    private static final int REQ_CODE_VIEW_STATISTICS = 102;
    private static final int REQ_CODE_VIEW_TRAINING_SESSION = 103;
    private static final int REQ_CODE_SETTINGS = 104;
    private static final int RESULT_ERROR_SAVE = 401;

    private static final String XML_FILE = "workout_logger_db.xml";
    private final String TAG = this.getClass().getSimpleName();

    protected static final String KEY_TRAINING_SESSION = "TRAINING_SESSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddTrainingSession);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ActivityNewTrainingSession.class);
                startActivityForResult(intent, REQ_CODE_ADD_TRAINING_SESSION);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        lstTrainingSessionSummary = (ListView)findViewById(R.id.LstTrainingSessionSummary);
        lstTrainingSessionSummary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ActivityTrainingSessionDetail.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_TRAINING_SESSION, listTrainingSessions.get(position));
                intent.putExtras(bundle);
                startActivityForResult(intent, REQ_CODE_VIEW_TRAINING_SESSION);
            }
        });

        workoutData = new WorkoutData(getApplicationContext());
        persistence = new XMLPersistence(workoutData);
        updateTrainingSessionList();
    }

    private void updateTrainingSessionList() {
        if (!workoutData.open()) {
            Toast.makeText(getApplicationContext(), R.string.open_db_error, Toast.LENGTH_LONG).show();
            finish();
        }
        try {
            listTrainingSessions = workoutData.getListTrainingSessions();
            CompletedTrainingSessionAdapter adapter =
                    new CompletedTrainingSessionAdapter(this, listTrainingSessions);
            lstTrainingSessionSummary.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_activity_menu_list_workouts:
                startActivityForResult(
                        new Intent(MainActivity.this, ActivityListWorkouts.class),
                        REQ_CODE_VIEW_STATISTICS);
                break;
            case R.id.main_activity_menu_load_xml:
                if (loadXML()) {
                    Toast.makeText(getApplicationContext(), R.string.main_activity_load_xml_ok,
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.main_activity_load_xml_error,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.main_activity_menu_save_xml:
                if (saveXML()) {
                    Toast.makeText(getApplicationContext(), R.string.main_activity_save_xml_ok,
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.main_activity_save_xml_error,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.main_activity_menu_settings:
                Log.d(TAG, "opening preferences");
                startActivityForResult(
                        new Intent(MainActivity.this, ActivityPreferences.class),
                        REQ_CODE_SETTINGS);
                break;
            case R.id.main_activity_menu_view_graph:
                startActivityForResult(
                        new Intent(MainActivity.this, ActivityViewStatistics.class),
                        REQ_CODE_VIEW_STATISTICS);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            case REQ_CODE_VIEW_STATISTICS:
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

    private boolean loadXML() {
        try {
            File pathSD = this.getExternalFilesDir(null);
            File file = new File(pathSD.getAbsolutePath(), XML_FILE);
            InputStream inputStream = new FileInputStream(file);
            boolean result = persistence.loadData(inputStream);
            Log.d(TAG, "Result of XML load: " + result);
            updateTrainingSessionList();
            lstTrainingSessionSummary.requestLayout();
            return result;
        } catch (IOException ioex) {
            Log.e(TAG, "I/O error loading XML file: " + ioex.getLocalizedMessage());
            return false;
        } catch (IllegalArgumentException iaex) {
            Log.e(TAG, "Illegal argument exception loading XML file: " + iaex.getLocalizedMessage());
            return false;
        }
    }

    private boolean saveXML() {
        try {
            File file = new File(this.getExternalFilesDir(null).getAbsolutePath(), XML_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(file);
            boolean result = persistence.saveData(outputStream);
            MediaScannerConnection.scanFile(
                    this, new String[] { file.getAbsolutePath() }, null, null);
            return result;

        } catch (IOException ioex) {
            Log.e(TAG, "Error saving XML file: " + ioex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        workoutData.close();
        super.onDestroy();
    }
}
