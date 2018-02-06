package eus.julenugalde.workoutlogger.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.controller.TrackArrayAdapter;
import eus.julenugalde.workoutlogger.model.Load;
import eus.julenugalde.workoutlogger.model.Track;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;
import eus.julenugalde.workoutlogger.model.WorkoutData;

public class ActivityViewStatistics extends AppCompatActivity {
    private static final int WIDTH_GRAPH = 350;
    private static final int HEIGHT_GRAPH = 330;
    private static final String KEY_ARRAY_WORKOUTS = "ARRAY_WORKOUTS";
    private static final String KEY_INDEX_WORKOUT = "INDEX_WORKOUT";
    private static final String KEY_INDEX_TRACK = "INDEX_TRACK";
    private static final String TAG = ActivityViewStatistics.class.getSimpleName();

    private WorkoutData workoutData;
    private Spinner cmbWorkouts;
    private Spinner cmbTracks;
    private WebView webViewGraph;

    private String[] arrayWorkouts;
    private Track[] arrayTracks;
    private int indexWorkout;
    private int indexTrack;
    private TrackArrayAdapter trackArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_statistics);

        if (savedInstanceState == null) {   //Activity created
            indexTrack = 0;
            indexWorkout = 0;
            workoutData = new WorkoutData(this);
            if (workoutData.open()) {
                ArrayList<Workout> listWorkouts = workoutData.getListWorkouts();
                initializeArrayWorkouts(listWorkouts);
                arrayTracks = loadTracks(0);
                workoutData.close();
            }
            else {
                arrayTracks = null;
            }
        }
        else {  //there are previous data
            arrayWorkouts = savedInstanceState.getStringArray(KEY_ARRAY_WORKOUTS);
            indexWorkout = savedInstanceState.getInt(KEY_INDEX_WORKOUT);
            if (workoutData.open()) {
                arrayTracks = loadTracks(indexWorkout);
                workoutData.close();
                indexTrack = savedInstanceState.getInt(KEY_INDEX_TRACK);
            }
            else {
                arrayTracks = null;
                indexTrack = 0;
            }
        }

        captureControls();
        initializeControls();
    }

    private void captureControls() {
        cmbWorkouts = (Spinner)findViewById(R.id.CmbStatisticsWorkouts);
        cmbTracks = (Spinner)findViewById(R.id.CmbStatisticsTracks);
        webViewGraph = (WebView)findViewById(R.id.WebViewStatisticsGraph);
    }

    private void initializeControls() {
        // Workouts spinner adapter
        ArrayAdapter<String> adapterWorkouts = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, arrayWorkouts);
        adapterWorkouts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbWorkouts.setAdapter(adapterWorkouts);
        cmbWorkouts.setSelection(indexWorkout);
        cmbWorkouts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != indexWorkout) {
                    indexWorkout = position;
                    indexTrack = 0;
                    updateArrayTracks();
                    updateGraph();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                indexTrack = 0;
                indexWorkout = 0;
                updateArrayTracks();
                updateGraph();
            }
        });

        //Tracks spinner adapter
        trackArrayAdapter = new TrackArrayAdapter(
                this, android.R.layout.simple_spinner_item, arrayTracks);
        trackArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbTracks.setAdapter(trackArrayAdapter);
        cmbTracks.setSelection(indexTrack);
        cmbTracks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != indexTrack) {
                    indexTrack = position;
                    updateGraph();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                indexTrack = 0;
                updateGraph();
            }
        });

        updateGraph();
    }

    private Track[] loadTracks(int index) {
        if (!workoutData.open()) {
            return new Track[] {new Track(getResources().getString(R.string.open_db_error))};
        }
        Track[] initial = workoutData.getWorkout(arrayWorkouts[index]).getTracks();
        workoutData.close();
        Track[] result = new Track[initial.length+1];
        //A "fictional" track is included in the first position
        result[0] = new Track(getResources().getString(R.string.statistics_global_index));
        for (int i=0; i<initial.length; i++) {
            result[i+1] = new Track(initial[i].getName());
        }
        return result;
    }

    private void initializeArrayWorkouts(ArrayList<Workout> workouts) {
        arrayWorkouts = new String[workouts.size()];
        int i = workouts.size() - 1;
        Iterator<Workout> iterator = workouts.iterator();
        while(iterator.hasNext()) {
            arrayWorkouts[i--] = iterator.next().getName();
        }
    }

    private void updateArrayTracks() {
        arrayTracks = loadTracks(indexWorkout);

        //TODO try to do this with notifyDataSetChanged()
        trackArrayAdapter = new TrackArrayAdapter(
                this, android.R.layout.simple_spinner_item, arrayTracks);
        trackArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbTracks.setAdapter(trackArrayAdapter);
        cmbTracks.setSelection(indexTrack);
    }

    private void updateGraph() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
            int numPoints;
            String[] dates;
            String[] values;
            String axisTitle;

            if (indexTrack == 0) {  //Calculate global index
                workoutData.open();
                Map<Date, Double> mapIndices =
                        workoutData.getIndicesWorkout(arrayWorkouts[indexWorkout]);
                workoutData.close();

                numPoints = mapIndices.size();
                dates = new String[numPoints];
                values = new String[numPoints];
                Set<Date> setDates = mapIndices.keySet();
                Iterator<Date> iterator = setDates.iterator();
                int i = 0;
                Date currentDate;
                while (iterator.hasNext()) {
                    currentDate = iterator.next();
                    dates[i] = sdf.format(currentDate);
                    values[i++] = mapIndices.get(currentDate).toString();
                }
                axisTitle = getResources().getString(R.string.statistics_global_index);
            }

            else {  //Each of the workout tracks
                Workout workout;
                Track[] aux;
                Load[] arrayLoads = new Load[0];
                String trackName = arrayTracks[indexTrack].getName();

                workoutData.open();
                ArrayList<TrainingSession> listTrainingSessions = workoutData.getListTrainingSessions();
                ArrayList<Workout> listWorkouts = new ArrayList<Workout>();
                ArrayList<String> listWorkoutDates = new ArrayList<String>();
                for (TrainingSession trainingSession : listTrainingSessions) {
                    if (trainingSession.getNameWorkout().equals(arrayWorkouts[indexWorkout])) {
                        Log.d(TAG, trainingSession.getNameWorkout() + "@" + trainingSession.getDate().toString());
                        listWorkouts.add(workoutData.getTrainingSession(
                                trainingSession.getNameWorkout(), trainingSession.getDate()));
                        listWorkoutDates.add(sdf.format(trainingSession.getDate()));
                    }
                }
                workoutData.close();

                numPoints = Math.min(listWorkoutDates.size(), listWorkouts.size());
                dates = new String[numPoints];
                values = new String[numPoints];

                for (int i=0; i<numPoints; i++) {
                    dates[i] = listWorkoutDates.get(i);
                    workout = listWorkouts.get(i);

                    //Retrieve the load value in the track
                    aux = workout.getTracks();

                    values[i] = "-1";   //Null value in the graphs
                    for (int j=0; j<aux.length; j++) {
                        if (aux[j].getName().equals(trackName)) {
                            arrayLoads = aux[j].getLoads();
                            //TODO for track with more that one load, only the first one is displayed
                            values[i] = arrayLoads[0].getKg() + "." + arrayLoads[0].getG();
                        }
                    }
                }

                axisTitle = getResources().getString(R.string.statistics_graph_no_title);
                if ((arrayLoads.length > 0) && (!arrayLoads[0].getName().equals(""))) {
                    axisTitle = arrayLoads[0].getName();
                }
            }

            String url = generateURL(axisTitle, dates, values);
            if (url != null) {
                webViewGraph.loadUrl(url);
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.statistics_error_data),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception ex) {
            Toast.makeText(this, getResources().getString(R.string.statistics_error_data) +
                            ":\n" + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected String generateURL(String loadNames, String[] dates, String[] values) {
        if (dates.length != values.length)
            return null;
        int i;
        int numPoints = Math.min(dates.length, values.length);
        double maxValue = 0;
        for (i=0; i<numPoints; i++){
            try {
                maxValue = Math.max(maxValue, Double.parseDouble(values[i]));
            } catch (NumberFormatException e) {}
        }
        long scaleY = Math.round(1.2*maxValue);

        StringBuilder sb = new StringBuilder ("http://chart.googleapis.com/chart?");
        sb.append ("chxl=0:");	//Elements X axis
        for (i=0; i<numPoints; i++)
            sb.append("|"+dates[i]);
        sb.append("&chxp=0");	//X axis
        for (i=0; i<numPoints; i++)
            sb.append(","+i);
        sb.append("&chxr=0,0," + (numPoints-1) + "|1,0," + scaleY);
        //sb.append("&chxr=0,0," + (numPoints-1));  //DEBUG
        sb.append("&chxt=x,y");		//Coordinate axes
        sb.append("&chs=" + WIDTH_GRAPH + "x" + HEIGHT_GRAPH);	//Graph size
        sb.append("&cht=lxy");			//define a function in two dimensions
        sb.append("&chco=3072F3");		//Line color
        sb.append("&chds=0," + (numPoints-1) + ",0," + scaleY);	//X and Y scales
        sb.append("&chd=t:-1|" + values[0]);	//Elements Y axis
        for (i=1; i<numPoints; i++) {
            if (values[i].equals(""))
                sb.append(",-1");
            else
                sb.append("," + values[i]);
        }
        sb.append("&chdl=" + loadNames);
        sb.append("&chdlp=b");	//Legend at the bottom of the chart, legend entries in a horizontal row
        //sb.append("&chls=2,4,1");	//Dotted line with width 2
        sb.append("&chls=3");	//Normal line with width 4
        sb.append("&chma=" + 10 + "," + 10 + "," + 10 + "," + 20);//Margin

        Log.d(TAG, sb.toString());  //DEBUG
        return sb.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(KEY_ARRAY_WORKOUTS, arrayWorkouts);
        outState.putInt(KEY_INDEX_TRACK, indexTrack);
        outState.putInt(KEY_INDEX_WORKOUT, indexWorkout);
    }
}
