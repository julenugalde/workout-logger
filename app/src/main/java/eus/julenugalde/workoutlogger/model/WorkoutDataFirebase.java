package eus.julenugalde.workoutlogger.model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WorkoutDataFirebase implements WorkoutData {
    DatabaseReference dbWorkouts;
    ArrayList<Workout> workouts;
    ArrayList<TrainingSession> trainingSessions;

    private static final String TAG = WorkoutDataFirebase.class.getSimpleName();

    protected WorkoutDataFirebase() {
        Log.i(TAG, "Opening Firebase database");
        workouts = new ArrayList<Workout>();
        trainingSessions = new ArrayList<TrainingSession>();
        trainingSessions.add(new TrainingSession(new Date(), 0, "kk", "kkkkkkkkkk"));
        dbWorkouts = FirebaseDatabase.getInstance().getReference(). child("info-workouts");
        dbWorkouts.addValueEventListener(new FirebaseDataChangeListener(workouts, trainingSessions));
    }

    @Override
    public boolean open() {
        readDB();
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public void cleanDB() {
        //TODO IMPLEMENT WRITING
    }

    @Override
    public void readDB() {
        Log.i(TAG, "----------- List of workouts -----------------");
        for (Workout workout : workouts) {
            Log.i(TAG, workout.toString());
        }
        Log.i(TAG, "------------- List of training sessions ------------");
        for (TrainingSession trainingSession : trainingSessions) {
            Log.i(TAG, trainingSession.toString());
        }
    }

    @Override
    public boolean existsWorkout(String workoutName) {
        for (Workout workout : workouts) {
            if (workout.getName().equals(workoutName)) return true;
        }
        return false;
    }

    @Override
    public boolean insertWorkout(Workout workout)
    {
        //TODO IMPLEMENT WRITING
        return false;
    }

    @Override
    public boolean deleteWorkout(String nameWorkout) {
        //TODO IMPLEMENT WRITING
        return false;
    }

    @Override
    public Workout getWorkout(String nameWorkout) {
        for (Workout workout : workouts) {
            if (workout.getName().equals(nameWorkout)) return workout;
        }
        return null;
    }

    @Override
    public ArrayList<Workout> getListWorkouts() {
        return workouts;
    }

    @Override
    public int getNumTrainingSessions(String nameWorkout) {
        return trainingSessions.size();
    }

    @Override
    public Workout getTrainingSession(String nameWorkout, Date date) {
        for (TrainingSession trainingSession : trainingSessions) {
            if ((trainingSession.getDate().equals(date)) &&
                    trainingSession.getNameWorkout().equals(nameWorkout)) {
                return getWorkout(trainingSession.getNameWorkout());
            }
        }
        return null;
    }

    @Override
    public boolean deleteTrainingSession(TrainingSession trainingSession) {
        //TODO IMPLEMENT WRITING DATABASE
        return false;
    }

    @Override
    public boolean insertTrainingSession(Workout workout, ArrayList<TrainingExercise> listExercises,
                                         String date, String comment) {
        //TODO IMPLEMENT WRITING DATABASE
        return false;
    }

    @Override
    public ArrayList<TrainingSession> getListTrainingSessions() throws Exception {
        return trainingSessions;
    }

    @Override
    public Map<Date, Double> getIndicesWorkout(String nameWorkout) {
        Map<Date,Double> result = new HashMap<Date,Double>();
        for (TrainingSession trainingSession : trainingSessions) {
            if (trainingSession.getNameWorkout().equals(nameWorkout)) {
                //TODO CALCULATE INDEX
                result.put(trainingSession.getDate(), new Double(100));
            }
        }
        return result;
    }
}

class FirebaseDataChangeListener implements ValueEventListener {
    private ArrayList<Workout> workouts;
    private ArrayList<TrainingSession> trainingSessions;
    private static final String TAG = FirebaseDataChangeListener.class.getSimpleName();

    protected FirebaseDataChangeListener
            (ArrayList<Workout> workouts, ArrayList<TrainingSession> trainingSessions) {
        this.trainingSessions = trainingSessions;
        this.workouts = workouts;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        workouts = new ArrayList<Workout>();
        trainingSessions = new ArrayList<TrainingSession>();

        try {
            Log.i(TAG, "------------ Listado de workouts --------------------");
            Iterable<DataSnapshot> iteratorWorkouts = dataSnapshot.child("workouts").getChildren();
            Iterable<DataSnapshot> iteratorTracks = dataSnapshot.child("tracks").getChildren();
            Iterable<DataSnapshot> iteratorLoads = dataSnapshot.child("loads").getChildren();
            Iterable<DataSnapshot> iteratorTS = dataSnapshot.child("entrenamientos").getChildren();
            Iterable<DataSnapshot> iteratorTE = dataSnapshot.child("ejercicios").getChildren();

            Workout workout;
            Track track;
            TrainingSession trainingSession;
            int idWorkout;
            int idTrack;
            int idTrainingSession;
            HashMap<String, String> valuesWorkouts;
            HashMap<String, String> valuesTracks;
            HashMap<String, String> valuesLoads;
            HashMap<String, String> valuesTrainingSessions;
            HashMap<String, String> valuesTrainingExercises;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (DataSnapshot itemWorkout : iteratorWorkouts) {
                //Info workout
                valuesWorkouts = (HashMap<String, String>) (itemWorkout.getValue());
                workout = new Workout(valuesWorkouts.get("nombreWorkout"));
                idWorkout = Integer.parseInt(itemWorkout.getKey());

                //List of tracks
                for (DataSnapshot itemTrack : iteratorTracks) {
                    valuesTracks = (HashMap<String, String>) (itemTrack.getValue());
                        /*Set<String> kk = valuesTracks.keySet();
                        for (String k : kk) {
                            Log.e(TAG, k + " - " + valuesTracks.get(k));
                        }*/
                    if (Integer.parseInt(valuesTracks.get("idWorkout")) == idWorkout) {
                        idTrack = Integer.parseInt(itemTrack.getKey());
                        track = new Track(valuesTracks.get("nombreTrack"));

                        //List of loads in the track
                        for (DataSnapshot itemLoad : iteratorLoads) {
                            valuesLoads = (HashMap<String, String>) (itemLoad.getValue());
                            if (Integer.parseInt(valuesLoads.get("idTrack")) == idTrack) {
                                track.addLoad(new Load(valuesLoads.get("nombreLoad")));
                            }
                        }
                        workout.addTrack(track);
                    }
                }
                Log.i(TAG, "idWorkout=" + idWorkout + " --> " + workout.toString());

                //Training sessions asociated to the workout
                for (DataSnapshot itemTrainingSession : iteratorTS) {
                    valuesTrainingSessions = (HashMap<String, String>)(itemTrainingSession.getValue());
                    if (Integer.parseInt(valuesTrainingSessions.get("idWorkout")) == idWorkout) {
                        idTrainingSession = Integer.parseInt(itemTrainingSession.getKey());
                        trainingSession = new TrainingSession(
                                sdf.parse(valuesTrainingSessions.get("fecha")),
                                idWorkout,
                                workout.getName(),
                                valuesTrainingSessions.get("comentario"));

                        //Exercises associated to the training session
                        /*for (DataSnapshot itemTrainingExercise : iteratorTE) {
                            valuesTrainingExercises =
                                    (HashMap<String,String>)(itemTrainingExercise.getValue());
                            if (Integer.parseInt(valuesTrainingExercises.get("idEntrenamiento")) ==
                                    idTrainingSession) {
                                xxxxxxxxxxxxxxxxxxxx
                            }
                        }*/

                        trainingSessions.add(trainingSession);
                        Log.i(TAG, "idTrainingSession=" + idTrainingSession + " --> " +
                            trainingSession.toString());
                    }
                }

                workouts.add(workout);
            }
            Log.i(TAG, "--------------------------------------------------------");
        } catch (java.text.ParseException pex) {
            Log.e(TAG, "Format error in database: " + pex.getLocalizedMessage());
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
