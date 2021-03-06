package eus.julenugalde.workoutlogger.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/** Implementation of the {@link WorkoutData} interface with a SQLite database */
public class WorkoutDataSQLite implements WorkoutData {
    /** Database file name */
    public static final String DB_FILE_NAME = "DBWorkouts";
    /** Database version */
    public static final int VERSION = 1;

	private static final String TAG = WorkoutDataSQLite.class.getSimpleName();
    private WorkoutLoggerSQLiteHelper workoutLoggerSQLiteHelper;
	protected SQLiteDatabase db;

    /** Constructor that creates the WorkoutDataSQLite object from the application context
     *
     * @param context Application context
     */
	protected WorkoutDataSQLite(Context context) {
		workoutLoggerSQLiteHelper =
				new WorkoutLoggerSQLiteHelper(context, DB_FILE_NAME, null, VERSION);
	}

    public boolean existsWorkout(String workoutName)  {
		String[] args = new String[]{workoutName};
		Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", args);
		boolean result = (c.getCount() > 0);
        c.close();
		return result;
	}

    public boolean open() {
		try {
        	db = workoutLoggerSQLiteHelper.getWritableDatabase();
			if (db==null) 
				Log.e(TAG, "getWritableDatabase() has returned null");
			return true;
		}
		catch (SQLiteException sqlex) {
			Log.e(TAG, "Error opening the database: " + sqlex.getMessage());
			db.close();
			return false;
		}
	}

    public void close() {
		db.close();
	}

    public boolean insertWorkout(Workout workout) {
		ContentValues newRecord = new ContentValues();
		try {
            //Save data into Workouts table
		    newRecord.put("nombreWorkout", workout.getName());
			long result = db.insert("Workouts", null, newRecord);
			if (result == -1)	//Ha habido un error
				return false;		
			//It's not guaranteed that the value of 'result' always matches with idWorkout (eventual
            //delete operations from the Workouts table must be taken into account), so the value
            //is retreived again from the table, event if this is unefficient.
			String[] args = new String[]{workout.getName()};
			Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", args); 
			c.moveToFirst();
			if (c.getCount() != 1) return false;
			int idWorkout = c.getInt(0);
			
			//Save data in the Tracks table
			int idTrack;
			Track[] arrayTracks = workout.getTracks();
			Load[] arrayLoads;
			for (int i=0; i<arrayTracks.length; i++) {
				newRecord.clear();	//nameWorkout is cleared
				newRecord.put("idWorkout", idWorkout);
				newRecord.put("nombreTrack", arrayTracks[i].getName());
				result = db.insert("Tracks", null, newRecord);
				if (result == -1){ return false;
				}
				args = new String[]{String.valueOf(idWorkout), arrayTracks[i].getName()};
				c = db.rawQuery("SELECT idTrack FROM Tracks WHERE (idWorkout=? AND nombreTrack=?)", args);
				if (c.getCount() != 1) return false;
				c.moveToFirst();
				idTrack = c.getInt(0);

				//Save data in the Loads table
				arrayLoads = arrayTracks[i].getLoads();				
				newRecord.clear();
				newRecord.put("idTrack", idTrack);
				if (arrayLoads!=null) {	//Track con cargas					
					for (int j=0; j<arrayLoads.length; j++) {
						newRecord.put("nombreLoad", arrayLoads[j].getName());
						result = db.insert("Loads", null, newRecord);
						if (result == -1) return false;
					}
				}
				//For a track without loads a record is also inserted in the Loads table. This
                // allows to show that the exercise has been completed in the Ejercicios table
				else {
                    //The string "-" will be used to indicate that there isn't any load
					newRecord.put("nombreLoad", "-");
					result = db.insert("Loads", null, newRecord);
				}					
			}
			return true;
		} catch (SQLiteException sqlex) {
			Log.e(TAG,"Error storing the workout in the database: " + sqlex.getMessage());
			return false;
		}
	}

    public ArrayList<Workout> getListWorkouts() {
		ArrayList<Workout> listWorkouts = new ArrayList<Workout>();
		Workout currentWorkout;
		Track currentTrack;
		Cursor cursorWorkouts = db.rawQuery("SELECT * FROM Workouts", null);
		Cursor cursorTracks = null;
		Cursor cursorLoads = null;
		String[] args;
		
		cursorWorkouts.moveToFirst();
		//Go over the workout list
		for (int i=0; i<cursorWorkouts.getCount(); i++) {
			currentWorkout = new Workout(cursorWorkouts.getString(1)); 	//nombreWorkout
			args = new String[] {String.valueOf(cursorWorkouts.getInt(0))};
			cursorTracks = db.rawQuery(
			        "SELECT idTrack, nombreTrack FROM Tracks WHERE idWorkout=?", args);
			cursorTracks.moveToFirst();
			// Go over the tracks list
			for (int j=0; j<cursorTracks.getCount(); j++) {
				currentTrack = new Track(cursorTracks.getString(1));	//nombreTrack
				args = new String[] {String.valueOf(cursorTracks.getInt(0))};	//idTrack
				cursorLoads = db.rawQuery("SELECT nombreLoad FROM Loads WHERE idTrack=?", args);
				if (cursorLoads.moveToFirst()) {	//Returns false if the load list is empty
					for (int k=0; k<cursorLoads.getCount(); k++) {
						currentTrack.addLoad(new Load(cursorLoads.getString(0)));
						cursorLoads.moveToNext();
					}				
				}
				currentWorkout.addTrack(currentTrack);
				cursorTracks.moveToNext();
			}
			//Retrieve the number of trainig sessions
			currentWorkout.setNumTrainingSessions(getNumTrainingSessions(currentWorkout.getName()));
			//Add to the list and continue
			listWorkouts.add(currentWorkout);
			cursorWorkouts.moveToNext();
		}
		if (cursorLoads != null) cursorLoads.close();
		if (cursorTracks != null) cursorTracks.close();
		cursorWorkouts.close();
		return listWorkouts;
	}

    public int getNumTrainingSessions(String nameWorkout) {
		String[] args = new String [] {nameWorkout};
		Cursor c = db.rawQuery("SELECT Entrenamientos.* FROM Entrenamientos " + 
			"INNER JOIN Workouts ON Workouts.idWorkout=Entrenamientos.idWorkout " + 
				"WHERE Workouts.nombreWorkout=?", args);
		return c.getCount();
	}
	
	public boolean deleteWorkout(String nameWorkout) {
		try {
			//Search the workout id
			String args[] = new String[] {nameWorkout};
			Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", args);
			if (c.getCount() != 1)
				return false;	//It doesn't exist or there's more than one --> error
			c.moveToFirst();
			int idWorkout = c.getInt(0);
			Log.d(TAG, "Attempting to delete workout with id " + idWorkout);

			//Seek and delete the associated training sessions. SQLite doesn't support INNER JOIN
            //in DELETE statements, so the training list is retreived in a Cursor object and the
            //training sessions are deleted one by one.
			c = db.rawQuery("SELECT Entrenamientos.idEntrenamiento FROM Entrenamientos " + 
				    "INNER JOIN Workouts ON Workouts.idWorkout=Entrenamientos.idWorkout " +
                    "WHERE Workouts.nombreWorkout=?", args);
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					//readDB();		//DEBUG
					//The session record is deleted from the 'Entrenamientos' table...
					db.execSQL("DELETE FROM Entrenamientos WHERE idEntrenamiento=" + c.getInt(0));
					//... and the associated exercises are also deleted
					db.execSQL("DELETE FROM Ejercicios WHERE idEntrenamiento="+c.getInt(0));
					c.moveToNext();
				}
			}
			
			//Deletes the workout
			int result = db.delete("Workouts", "idWorkout="+idWorkout, null);
			if (result != 1) {
			    Log.d(TAG, "Error deleting workout with id " + idWorkout);
			    return false;
            }
			
			//Delete the tracks in the workout
			int idTrack;
			Cursor c1;
			args[0] = String.valueOf(idWorkout);
			c = db.rawQuery("SELECT idTrack FROM Tracks WHERE idWorkout=?", args);
			if (c.getCount()==0)
				Log.e(TAG, "The workout didn't have any tracks");
			else {
				if (c.moveToFirst()) {
					do {
						idTrack = c.getInt(0);					
						//Delete the loads in the track
						args[0] = String.valueOf(idTrack);
						c1 = db.rawQuery("SELECT idLoad FROM Loads WHERE idTrack=?", args);
						if (c1.moveToFirst()) {
							do {
								db.delete("Loads", "idLoad="+c1.getInt(0), null);
							} while (c1.moveToNext());
						}					
						db.delete("Tracks", "idTrack="+idTrack, null);
					} while (c.moveToNext());
				}
			}
			c.close();
			return true;
			
		} catch (SQLiteException sqlex) {
			Log.e(TAG, "Database error deleting workout: " + sqlex.getMessage());
			return false;
		}
	}

    public ArrayList<TrainingSession> getListTrainingSessions() throws Exception {
		ArrayList<TrainingSession> listTrainingSessions = new ArrayList<TrainingSession>();
		//Retreive the training sessions ordered by date
		Cursor cursorTrainingSessions = db.rawQuery(
		        "SELECT * FROM Entrenamientos ORDER BY fecha DESC", null);
		Log.d(TAG, cursorTrainingSessions.getCount()+
                " training session records in the database");
		Cursor cursorWorkouts = null;
		//The 'yyyy-MM-dd' format is used for the date in the database
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] args = new String[]{""}; 	
		Date date;
		cursorTrainingSessions.moveToFirst();
		//For each training session, the workout information is retreived
		while (!cursorTrainingSessions.isAfterLast()) {
			args[0] = String.valueOf(cursorTrainingSessions.getInt(1)); //idWorkout
			cursorWorkouts = db.rawQuery("SELECT nombreWorkout FROM Workouts WHERE idWorkout=?", args);
			cursorWorkouts.moveToFirst();
			if (cursorWorkouts.getCount()==0)
				throw new Exception("Database error: idWorkout value (" + args[0] +
                        ") doesn't match any record in the workouts table");
			else {				
				try {
					date = formatter.parse(cursorTrainingSessions.getString(2));    //fecha
				} catch (ParseException pex) {
					throw new Exception ("Error in the date format. Unable to parse");
				}
				listTrainingSessions.add(new TrainingSession(
				        date, //date
                        cursorTrainingSessions.getInt(1), //idWorkout
                        cursorWorkouts.getString(0), //nameWorkout
                        cursorTrainingSessions.getString(3))); //comment
			}
			cursorTrainingSessions.moveToNext();
		}
		cursorTrainingSessions.close();
		if (cursorWorkouts != null) cursorWorkouts.close();
        return listTrainingSessions;
	}

    public Workout getTrainingSession(String nameWorkout, Date date) {
		try {
			Workout result = new Workout(nameWorkout);
			Track track = new Track();
			Load[] arrayLoads;
			int idTrainingSession = 0;
			int idTrack = 0;
			int[] idLoad;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

			//Retrieve the training sessions ids associated to the workout and date
            String[] args = new String[] {nameWorkout, sdf.format(date)};
            Cursor c = db.rawQuery(
			        "SELECT Entrenamientos.idEntrenamiento FROM Entrenamientos " +
                    "INNER JOIN Workouts ON Workouts.idWorkout=Entrenamientos.idWorkout "+
                    "WHERE (Workouts.nombreWorkout=? AND Entrenamientos.fecha=?)", args);

			if (!c.moveToFirst()) return null;
			idTrainingSession = c.getInt(0);

            //Retrieve the info of exercises carried out in the training session
			args = new String[]{String.valueOf(idTrainingSession)};
			c = db.rawQuery("SELECT * FROM Ejercicios WHERE idEntrenamiento=?", args);
			if (c.moveToFirst()) {
				Cursor cLoads;
				arrayLoads = new Load[c.getCount()];
				idLoad = new int[c.getCount()];
				int i=0;
				do {
                    //Load information is retrieved from the DB and stored in an array
                    cLoads = db.rawQuery("SELECT nombreLoad FROM Loads WHERE idLoad=?",
							new String[]{String.valueOf(c.getInt(1))});
					if (cLoads.moveToFirst()) {
						idLoad[i] = c.getInt(1);
						arrayLoads[i++] = new Load(
						        cLoads.getString(0), //nombreLoad
                                c.getInt(1), //idLoad
                                c.getInt(3), //kg
                                c.getInt(4)); //g
					}
					else throw new SQLiteException (
							"Integrity error in the database: idLoad doesn't exist in table Loads");
				} while (c.moveToNext());
				if (cLoads != null) cLoads.close();

                //For each load, get the associated track information
				for (i=0; i<idLoad.length; i++) {					
					c = db.rawQuery(
					        "SELECT Tracks.idTrack, Tracks.nombreTrack, Loads.nombreLoad FROM Tracks "+
							"INNER JOIN Loads ON Tracks.idTrack=Loads.idTrack " +
                            "WHERE Loads.idLoad=?",
							new String[]{String.valueOf(idLoad[i])});
					if (c.moveToFirst()) {
                        //New track. Empty object had already been created. Track name stored
					    if (i==0) {
							track.setName(c.getString(1));
							track.addLoad(arrayLoads[i]);
							idTrack = c.getInt(0);
						}
						//If the idTrack value is the same as the previous one, the load information
                        //is stored in the current track
						else if (i!=0 && idTrack==c.getInt(0))
							track.addLoad(arrayLoads[i]);
						//New track. The previous one is stored and a new one is created
					    else {
							result.addTrack(track);
							track = new Track(c.getString(1));
							track.addLoad(arrayLoads[i]);
							idTrack = c.getInt(0);
						}
						//Last load. The track is stored.
						if (i==(idLoad.length-1))
							result.addTrack(track);
					}
					else throw new SQLiteException(
							"Database integrity error: foreign key idTrack not found in table Tracks");
				}
			}
			c.close();
			return result;
		} catch (SQLiteException sqlex) {
		    Log.e(TAG, sqlex.getLocalizedMessage());
			return null;
		}
	}

    public boolean deleteTrainingSession(TrainingSession trainingSession) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String[] args = new String[] {
		        sdf.format(trainingSession.getDate()),
                String.valueOf(trainingSession.getIdWorkout())};
		Cursor c = db.rawQuery(
			    "SELECT idEntrenamiento FROM Entrenamientos " +
                    "WHERE Entrenamientos.fecha=? AND Entrenamientos.idWorkout=?", args);

        //Only one record can exist. If there's more than one, only the first is deleted
		if (c.moveToFirst()) {
			int result = db.delete(
			        "Entrenamientos", "idEntrenamiento="+c.getInt(0), null);
			if (result != 1) return false;
			result = db.delete(
			        "Ejercicios", "idEntrenamiento="+c.getInt(0), null);
			return (result>0);
		}
		else {  //Not found
			return false;
		}
	}

    public Workout getWorkout(String nameWorkout) {
		Workout workout = new Workout (nameWorkout);
		Track track;
		//Get the workout id from the database
		Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", new String[] {nameWorkout});
		if (c.getCount() != 1)	return null;	//Not found or more than one
		c.moveToFirst();
		int idWorkout = c.getInt(0);
		Log.d(TAG, "The id of the selected workout is " + idWorkout);
		
		c = db.rawQuery(
		        "SELECT * FROM Tracks WHERE idWorkout=?", new String[]{String.valueOf(idWorkout)});
		if (c.moveToFirst()) {	//There should be at least one track
			Cursor cLoads;
			//Find all the tracks and add them to the workout
			do {
				track = new Track(c.getString(2));
				cLoads = db.rawQuery(
				        "SELECT * FROM Loads WHERE idTrack=?",
                        new String[]{String.valueOf(c.getInt(0))});
				if (cLoads.moveToFirst()) {
					Cursor cExercises;
				    //Find all the loads and add them to the workout
					do {
						cExercises = db.rawQuery(
								"SELECT * FROM Ejercicios WHERE idLoad=?",
                                new String[]{String.valueOf(cLoads.getInt(0))});
						//If there's any training session associated in the DB, the last load value
                        //is taken, to be displayed as default value
						if (cExercises.moveToLast())
							track.addLoad(new Load(
							        cLoads.getString(2), //nombreLoad (name)
                                    cLoads.getInt(0), //idLoad
                                    cExercises.getInt(3), //kg
                                    cExercises.getInt(4))); //g
						//If there isn't any training session, 1kg is used as default value
                        else {
                            track.addLoad(new Load(
                                    cLoads.getString(2),
                                    cLoads.getInt(0),
                                    1,
                                    0));
                        }
					} while (cLoads.moveToNext());
                    cExercises.close();
				}
				workout.addTrack(track);
			} while (c.moveToNext());
            cLoads.close();
		}
		else return null;

		c.close();
        return workout;
	}

    @SuppressLint("UseSparseArrays")
    public Map<Date, Double> getIndicesWorkout (String nameWorkout) {
        Map<Date, Double> result = new TreeMap<Date, Double>();

        Cursor c = db.rawQuery(
                "SELECT idWorkout FROM Workouts WHERE nombreWorkout=?",
                new String[] {nameWorkout});
        if (c.getCount() != 1)	return null;	//Doesn't exist or more than one --> error
        c.moveToFirst();
        int idWorkout = c.getInt(0);

        //Seek training sessions and get load data
        Cursor cAux;
        double weightValue;
        int idLoad;
        ArrayList<Double> alAux;
        //Retrieve all training sessions associated with the workout
        c = db.rawQuery(
                "SELECT * FROM Entrenamientos WHERE idWorkout=?",
                new String[] {String.valueOf(idWorkout)});
        if (!c.moveToFirst()) { //No training session associated to the workout
            return result;
        }
        Map<Integer,ArrayList<Double>> mapWeights = new HashMap<Integer,ArrayList<Double>>();
        do {    //Loop for each training session
            //Retrieve exercises in the training session
            cAux = db.rawQuery("SELECT * FROM Ejercicios WHERE idEntrenamiento=?",
                    new String[] {String.valueOf(c.getInt(0))});
            //mapWeights will store the weight values for calculating the average. The key is idLoad
            cAux.moveToFirst();
            do {    //Loop for each exercise
                try {
                    idLoad = cAux.getInt(1);
                    weightValue = Double.parseDouble(cAux.getInt(3) + "." + cAux.getInt(4));
                } catch(NumberFormatException ex) {
                    idLoad = -1;
                    weightValue = 0;
                }
                if (mapWeights.containsKey(Integer.valueOf(idLoad))) {
                    alAux = mapWeights.get(Integer.valueOf(idLoad));
                    alAux.add(Double.valueOf(weightValue));
                }
                else {
                    alAux = new ArrayList<Double>();
                    alAux.add(Double.valueOf(weightValue));
                    mapWeights.put(idLoad, alAux);
                }

            } while (cAux.moveToNext());
        } while (c.moveToNext());

        Map<Integer,Double> mapAverages = new HashMap<Integer,Double>();
        int numTotal;
        double sum;
        Set<Integer> setLoads = mapWeights.keySet();
        for (Integer keyLoad:setLoads) {
            alAux = mapWeights.get(keyLoad);
            numTotal = alAux.size();
            sum = 0;
            for (int i=0; i<numTotal; i++)
                sum += alAux.get(i).doubleValue();
            mapAverages.put(keyLoad, Double.valueOf(sum/numTotal));
        }

        //Each training session is compared with the averages
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        double indexLoad, averageCurrentLoad;
        Date date;
        c.moveToFirst();
        do {
            indexLoad = 100;
            try {
                date = sdf.parse(c.getString(2));
            } catch (ParseException e) {
                Log.e(TAG, "Error in the date format: " + e.getLocalizedMessage());
                return null;
            }

            cAux = db.rawQuery(
                    "SELECT * FROM Ejercicios WHERE idEntrenamiento=?",
                    new String[] {String.valueOf(c.getInt(0))});
            //Weight values are stored in the map to calculate the average. The key is idLoad
            cAux.moveToFirst();
            do {
                try {
                    idLoad = cAux.getInt(1);
                    weightValue = Double.parseDouble(cAux.getInt(3) + "." + cAux.getInt(4));
                    averageCurrentLoad = mapAverages.get(Integer.valueOf(idLoad));
                    if (averageCurrentLoad != 0)
                        indexLoad *= (weightValue/averageCurrentLoad);
                } catch(NumberFormatException ex) {
                    Log.e(TAG, "Parse error: " + cAux.getInt(3) + "." + cAux.getInt(4));
                    idLoad = -1;
                    indexLoad = 0;
                }
            } while(cAux.moveToNext());

            result.put(date, Double.valueOf(indexLoad));
        } while (c.moveToNext());

        c.close();
        cAux.close();
        return result;
    }

    public boolean insertTrainingSession(Workout workout, ArrayList<TrainingExercise> listExercises,
                                         String date, String comment) {
        //Retrieve the idWorkout from the database
        Cursor c = db.rawQuery(
                "SELECT idWorkout FROM Workouts WHERE nombreWorkout=?",
                new String[] {workout.getName()});
        if (c.getCount() != 1) {
            Log.e(TAG, "Error storing training session: workout doesn't exist or multiple");
            return false;
        }
        c.moveToFirst();
        int idWorkout = c.getInt(0);

        //Insert the training session into table 'Entrenamientos'
        ContentValues contentValues = new ContentValues();
        contentValues.put("idWorkout", idWorkout);
        contentValues.put("fecha", date);
        contentValues.put("comentario", comment);
        long rowId = db.insert("Entrenamientos", null, contentValues);
        int idTrainingSession = (int)rowId;
        if (idTrainingSession == -1) {
            Log.e(TAG, "Error inserting into training session table");
            return false;
        }

        //Insert each track with completed=true into table 'Ejercicios'
        int idTrack=0;
        int idLoad=0;
        for (TrainingExercise exercise : listExercises) {
            if (exercise.isCompleted()) {
                //Find the track id
                c = db.rawQuery("SELECT * FROM Tracks WHERE idWorkout=?",
                        new String[] {String.valueOf(idWorkout)});
                if (c.moveToFirst()) {
                    do { //Check track name
                        if (c.getString(2).compareTo(exercise.getName()) == 0)
                            idTrack = c.getInt(0);
                    } while(c.moveToNext());
                }
                else {
                    Log.e(TAG, "Error storing training session: workout without tracks");
                    return false;
                }
                if (idTrack==0) {
                    Log.e(TAG, "Error storing training session: incorrect track id");
                    return false;
                }

                //Find the loads associated to the track
                c = db.rawQuery("SELECT * FROM Loads WHERE idTrack=?",
                        new String[] {String.valueOf(idTrack)});
                if (!c.moveToFirst()) {
                    Log.e(TAG, "Integrity error in the database: record in Loads doesn't exist");
                    return false;
                }
                else if (c.getString(2).equals("-")) {	//No loads in the track
                    idLoad = c.getInt(0);
                    contentValues.clear();
                    contentValues.put("idLoad", idLoad);
                    contentValues.put("idEntrenamiento", idTrainingSession);
                    //contentValues.put("kg", 0);
                    //contentValues.put("g", 0);
                    if (db.insert("Ejercicios", null, contentValues) == -1) {
                        Log.e(TAG, "Error inserting training exercise into the database");
                        return false;
                    }
                }
                else {  //Save each load in the track
                    int i=0;
					do {
                        if (!exercise.getLoadName(i).isEmpty()) {
                            idLoad = c.getInt(0);
                            contentValues.clear();
                            contentValues.put("idLoad", idLoad);
                            contentValues.put("idEntrenamiento", idTrainingSession);
                            contentValues.put("kg", exercise.getLoadKg(i));
                            contentValues.put("g", exercise.getLoadG(i));
                            if (db.insert("Ejercicios", null, contentValues) == -1) {
                                Log.e(TAG, "Error inserting training exercise into the database");
                                return false;
                            }
                        }
                        i++;
                    } while (c.moveToNext());
                }
            }
        }
        return true;
    }


    public void cleanDB() {
        db.execSQL("DELETE FROM Workouts");
        db.execSQL("DELETE FROM Tracks");
        db.execSQL("DELETE FROM Loads");
        db.execSQL("DELETE FROM Entrenamientos");
        db.execSQL("DELETE FROM Ejercicios");
    }

    public void readDB() {
        Cursor c = db.rawQuery("SELECT * FROM Workouts", null);
        c.moveToFirst();
        Log.i(TAG, "-- Workouts ----------------------------------------------");
        Log.i(TAG, "idWorkout nombreWorkout");
        for (int i=0; i<c.getCount(); i++) {
            Log.i(TAG, c.getInt(0) + "        " + c.getString(1));
            c.moveToNext();
        }

        c = db.rawQuery("SELECT * FROM Tracks", null);
        c.moveToFirst();
        Log.i(TAG, "-- Tracks ------------------------------------------------");
        Log.i(TAG, "idTrack idWorkout nombreTrack");
        for (int i=0; i<c.getCount(); i++) {
            Log.i(TAG, c.getInt(0) + "      " + c.getInt(1) + "        "+ c.getString(2));
            c.moveToNext();
        }

        c = db.rawQuery("SELECT * FROM Loads", null);
        c.moveToFirst();
        Log.i(TAG, "-- Loads -------------------------------------------------");
        Log.i(TAG, "idLoad idTrack nombreLoad");
        for (int i=0; i<c.getCount(); i++) {
            Log.i(TAG, c.getInt(0) + "      " + c.getInt(1) + "       "+ c.getString(2));
            c.moveToNext();
        }

        c = db.rawQuery("SELECT * FROM Entrenamientos", null);
        c.moveToFirst();
        Log.i(TAG, "-- Entrenamientos -------------------------------------------------");
        Log.i(TAG, "idEntrenamienot fecha idWorkout comentario");
        for (int i=0; i<c.getCount(); i++) {
            Log.i(TAG, c.getInt(0) + "      " + c.getString(1)
                    + "       " + c.getInt(2) + "       "+ c.getString(3));
            c.moveToNext();
        }

        c = db.rawQuery("SELECT * FROM Ejercicios", null);
        c.moveToFirst();
        Log.i(TAG, "-- Ejercicios -------------------------------------------------");
        Log.i(TAG, "idEjercicio idLoad idEntrenamiento kg g");
        for (int i=0; i<c.getCount(); i++) {
            Log.i(TAG, c.getInt(0) + "    " + c.getInt(1) + "       "
                    + c.getInt(2) + " " + c.getInt(3) + " " + c.getInt(4));
            c.moveToNext();
        }
        Log.i(TAG, "----------------------------------------------------------");
    }
}
