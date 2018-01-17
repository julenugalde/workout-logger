package eus.julenugalde.workoutlogger.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/** Helper class to manage the SQLite database creation */
public class WorkoutLoggerSQLiteHelper extends SQLiteOpenHelper {
	
	private String sqlCreateWorkouts = 
			"CREATE TABLE Workouts (idWorkout INTEGER PRIMARY KEY, nombreWorkout TEXT)";
	private String sqlCreateTracks = 
			"CREATE TABLE Tracks (idTrack INTEGER PRIMARY KEY, idWorkout INTEGER, nombreTrack TEXT)";
	private String sqlCreateLoads = 
			"CREATE TABLE Loads (idLoad INTEGER PRIMARY KEY, idTrack INTEGER, nombreLoad TEXT)";
	private String sqlCreateEntrenamientos =
			"CREATE TABLE Entrenamientos (idEntrenamiento INTEGER PRIMARY KEY, idWorkout INTEGER, " + 
			"fecha TEXT, comentario TEXT)";
	private String sqlCreateEjercicios = 
			"CREATE TABLE Ejercicios (idEjercicio INTEGER PRIMARY KEY, idLoad INTEGER, " + 
			"idEntrenamiento INTEGER, kg INTEGER, g INTEGER)";

    /** Constructor to create a helper object. Calls the SQLiteOpenHelper constructor.
     *
     * @param context Application context
     * @param name Database file name, or null for in-memory database
     * @param factory To create cursor objects when calling query
     * @param version Database version number, starting at 1
     */
	public WorkoutLoggerSQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sqlCreateWorkouts);
		db.execSQL(sqlCreateTracks);
		db.execSQL(sqlCreateLoads);
		db.execSQL(sqlCreateEntrenamientos);
		db.execSQL(sqlCreateEjercicios);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
