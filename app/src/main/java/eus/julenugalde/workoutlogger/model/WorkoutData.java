package eus.julenugalde.workoutlogger.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Xml;

/** This class manages the database transactions for the application. It provides methods to open
 * and close the database, load/store/delete workout information (including associated tracks and
 * loads), load/store/delete training sessions information (including training exercises).
 * It also provides methods to import/export information from/to an XML file.
 */
public class WorkoutData {
    /** Database file name */
    public static final String DB_FILE_NAME = "DBWorkouts";
    /** Database version */
    public static final int VERSION = 1;

    private WorkoutLoggerSQLiteHelper workoutLoggerSQLiteHelper;
	private SQLiteDatabase db;

    /** Constructor that creates the WorkoutData object from the application context
     *
     * @param context Application context
     */
	public WorkoutData(Context context) {
		workoutLoggerSQLiteHelper =
				new WorkoutLoggerSQLiteHelper(context, DB_FILE_NAME, null, VERSION);
	}

    /** Checks if the workout to be stored exists
     *
     * @param workout Workout to be stored
     * @return true if the workout exists
     */
	public boolean existsWorkout(Workout workout)  {
		String[] args = new String[]{workout.getName()};
		Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", args);
		boolean result = (c.getCount() > 0);
		//TODO añadido codigo - COMPROBAR QUE se puede close sin problemas
        c.close();
        //////////////////////////////
		return result;
	}

    /** Opens the database. In case of error, a message is logged
     *
     * @return true if successful (even if the SQLiteDatabase object retreived is null), false
     * if an SQLiteException occurs.
     */
	public boolean open() {
		try {
			db = workoutLoggerSQLiteHelper.getWritableDatabase();
			if (db==null) 
				Log.e("WorkoutData", "getWritableDatabase() has returned null");
			return true;
		}
		catch (SQLiteException sqlex) {
			Log.e("WorkoutData", "Error opening the database: " + sqlex.getMessage());
			db.close();
			return false;
		}
	}

    /** Closes the database */
	public void close() {
		db.close();
	}

    /** Inserts a workout information into the database
     *
     * @param workout Workout to be stored in the database
     * @return true if successful, false otherwise
     */
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
			//TODO ver si esto es necesario y si no, borrar
			readDB();
			/////////////////////////////////////////
			return true;
		} catch (SQLiteException sqlex) {
			Log.e("WorkoutData",
                    "Error storing the workout in the database: " + sqlex.getMessage());
			return false;
		}
	}

    /** Recovers the list of workouts from the database
     *
     * @return Array list with all the workouts stored in the database
     */
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
			listWorkouts.add(currentWorkout);
			cursorWorkouts.moveToNext();
		}
		// TODO Codigo nuevo - Comprobar que se se cierran bien ////////
        if (cursorLoads != null) cursorLoads.close();
		if (cursorTracks != null) cursorTracks.close();
		cursorWorkouts.close();
		///////////////////////////////////////////////////////////
		return listWorkouts;
	}

    /** Returns the number of stored training sessions for a given workout
     *
     * @param nameWorkout Name of the workout
     * @return Number of training sessions stored in the database
     */
	public int getNumTrainingSessions(String nameWorkout) {
		String[] args = new String [] {nameWorkout};
		Cursor c = db.rawQuery("SELECT Entrenamientos.* FROM Entrenamientos " + 
			"INNER JOIN Workouts ON Workouts.idWorkout=Entrenamientos.idWorkout " + 
				"WHERE Workouts.nombreWorkout=?", args);
		return c.getCount();
	}
	
	/** Deletes a workout from the database. It also deletes all the training sessions associated
     * to the workout.
     *
     * @param nameWorkout Name of the workout to be deleted, e.g. "BodyPump 85"
	 * @return true if it's correctly deleted; false if there's any error, the workout doesn't
     * exist or more than one workout is stored with the same name.
	 */
	public boolean deleteWorkout(String nameWorkout) {
		try {
			//Search the workout id
			String args[] = new String[] {nameWorkout};
			Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", args);
			if (c.getCount() != 1)
				return false;	//It doesn't exist or there's more than one --> error
			c.moveToFirst();
			int idWorkout = c.getInt(0);
			Log.d("WorkoutLogger", "Attempting to delete workout with id " + idWorkout);

			//Seek and delete the associated training sessions. SQLite doesn't support INNER JOIN
            //in DELETE statements, so the training list is retreived in a Cursor object and the
            //training sessions are deleted one by one.
			c = db.rawQuery("SELECT Entrenamientos.idEntrenamiento FROM Entrenamientos " + 
				    "INNER JOIN Workouts ON Workouts.idWorkout=Entrenamientos.idWorkout " +
                    "WHERE Workouts.nombreWorkout=?", args);
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					readDB();
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
			    Log.d("WorkoutLogger", "Error deleting workout with id " + idWorkout);
			    return false;
            }
			
			//Delete the tracks in the workout
			int idTrack;
			Cursor c1;
			args[0] = String.valueOf(idWorkout);
			c = db.rawQuery("SELECT idTrack FROM Tracks WHERE idWorkout=?", args);
			if (c.getCount()==0)
				Log.e("WorkoutData", "The workout didn't have any tracks");
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
			//TODO codigo nuevo. ver si se cierra bien
            c.close();
			////////////////////////////////////////
			return true;
			
		} catch (SQLiteException sqlex) {
			Log.e("WorkoutData", "Database error deleting workout: " + sqlex.getMessage());
			return false;
		}
	}

    /** Returns the list of training sessions stored in the database
     *
     * @return Array list with all the training sessions stored in the database
     * @throws Exception
     */
	public ArrayList<TrainingSession> getListTrainingSessions() throws Exception {
		ArrayList<TrainingSession> listTrainingSessions = new ArrayList<TrainingSession>();
		//Retreive the training sessions ordered by date
		Cursor cursorTrainingSessions = db.rawQuery(
		        "SELECT * FROM Entrenamientos ORDER BY fecha", null);
		Log.d("WorkoutData", cursorTrainingSessions.getCount()+
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
		//TODO codigo nuevo. comprobar que va bien
		cursorTrainingSessions.close();
		if (cursorWorkouts != null) cursorWorkouts.close();
        //////////////////////////////
		return listTrainingSessions;
	}

    /** Retrieves the training session information based on the workout name and the date
     *
     * @param nameWorkout Name of the workout carried out in the session
     * @param date Training session date
     * @return If found, training session information; null if not found
     */
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

			//TODO Devolver null si la búsqueda no ha devuelto ningún registro
			c.moveToFirst();
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
				//TODO codigo nuevo. comprobar que se cierra bien el cursor
                if (cLoads != null) cLoads.close();
                /////////////////////////////

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
			//TODO codigo nuevo. comprobar que se cierra bien
            c.close();
			////////////////////////////////
			return result;
		} catch (SQLiteException sqlex) {
		    Log.e("WorkoutData", sqlex.getLocalizedMessage());
			return null;
		}
	}

    /** Deletes a training session from the database
     *
     * @param trainingSession Training session to be deleted
     * @return true if the training session was found and successfully deleted; false if there's
     * any error or the session wasn't found in the database
     */
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

    /** Retrieves the workout information based on the name
     *
     * @param nameWorkout Name of the workout carried out in the session
     * @return If found, workout information; null if not found or error
     */
    public Workout getWorkout(String nameWorkout) {
		Workout workout = new Workout (nameWorkout);
		Track track;
		//Get the workout id from the database
		Cursor c = db.rawQuery("SELECT idWorkout FROM Workouts WHERE nombreWorkout=?", new String[] {nameWorkout});
		if (c.getCount() != 1)	return null;	//Not found or more than one
		c.moveToFirst();
		int idWorkout = c.getInt(0);
		//Log.d("JULEN", "El id del workout seleccionado es " + idWorkout);
		
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
                    //TODO codigo nuevo. comprobar que se cierra bien
                    cExercises.close();
                    /////////////////////////////////////
				}
				workout.addTrack(track);
			} while (c.moveToNext());
            //TODO codigo nuevo. comprobar que se cierra bien
            cLoads.close();
            /////////////////////////////////////
		}
		else return null;

		//TODO codigo nuevo. comprobar que se cierra bien
		c.close();
        /////////////////////////////////////
		return workout;
	}
	
}
