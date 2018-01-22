package eus.julenugalde.workoutlogger.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lstTrainingSessionSummary = (ListView)findViewById(R.id.LstTrainingSessionSummary);
        lstTrainingSessionSummary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                //TODO Crear activitytrainingsessiondetail
                /*Intent intent = new Intent(MainActivity.this, ActivityTrainingSessionDetail.class);
                Bundle bundle = new Bundle();
                int index = listTrainingSessions.size()-position-1; //Showing in inverse order
                intent.putExtras(bundle);
                startActivityForResult(intent, REQ_CODE_VIEW_TRAINING_SESSION);*/
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
            listTrainingSessions = workoutData.getListTrainingSessionsDEBUG();  //Todo quitaar DEBUG
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
        //TODO Include menu?
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
