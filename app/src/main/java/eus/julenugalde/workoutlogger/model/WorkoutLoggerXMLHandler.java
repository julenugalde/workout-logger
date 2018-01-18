package eus.julenugalde.workoutlogger.model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/** Handler class for the XML file that contains the workout registries. Database information
 * is loaded using an SQLiteDatabase object.
 */
public class WorkoutLoggerXMLHandler extends DefaultHandler {
	private StringBuilder sb;
	private SQLiteDatabase db;
	private int idWorkout = 0;
	private int idTrack = 0;
	private int idLoad = 0;
	private int idSession = 0;
	private int idExercise = 0;
	private ContentValues contentValues;
    private String aux = "";

    /** Constructor that creates the handler
     *
     * @param db SQLite database to be used
     */
	public WorkoutLoggerXMLHandler(SQLiteDatabase db) {
		this.db = db;
		sb = new StringBuilder();
		contentValues = new ContentValues();
	}
	
	@Override
	public void startDocument() throws SAXException {
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
		//super.startElement(uri, localName, qName, attributes);
		sb.setLength(0);
		if (localName.equals("workout")){
			idWorkout = Integer.parseInt(attributes.getValue("idWorkout"));
		}
		else if (localName.equals("track")) {
			idTrack = Integer.parseInt(attributes.getValue("idTrack"));
			idWorkout = Integer.parseInt(attributes.getValue("idWorkout"));		
		}
		else if (localName.equals("load")) {
			idLoad = Integer.parseInt(attributes.getValue("idLoad"));
			idTrack = Integer.parseInt(attributes.getValue("idTrack"));		
		}
		else if (localName.equals("entrenamiento")) {
			idSession = Integer.parseInt(attributes.getValue("idSession"));
			idWorkout = Integer.parseInt(attributes.getValue("idWorkout"));		
		}
		else if (localName.equals("ejercicio")) {
			idExercise = Integer.parseInt(attributes.getValue("idExercise"));
			idLoad = Integer.parseInt(attributes.getValue("idLoad"));
			idSession = Integer.parseInt(attributes.getValue("idSession"));
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		//super.characters(ch, start, length);
		sb.append(ch, start, length);
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		//super.endElement(uri, localName, qName);
        if (localName.equals("workout")) {
			contentValues.clear();
			contentValues.put("idWorkout", idWorkout);
			contentValues.put("nombreWorkout", sb.toString());
			db.insert("Workouts", null, contentValues);
		}
		else if (localName.equals("track")) {
			contentValues.clear();
			contentValues.put("idTrack", idTrack);
			contentValues.put("idWorkout", idWorkout);
			contentValues.put("nombreTrack", sb.toString());
			db.insert("Tracks", null, contentValues);
		}
		else if (localName.equals("load")) {
			contentValues.clear();
			contentValues.put("idLoad", idLoad);
			contentValues.put("idTrack", idTrack);
			contentValues.put("nombreLoad", sb.toString());
			db.insert("Loads", null, contentValues);
		}
		else if (localName.equals("fecha")) {	//There are 2 elements in "entrenamiento"
			aux = sb.toString();
			sb.setLength(0);
		}
		else if (localName.equals("entrenamiento")) {
			contentValues.clear();
			contentValues.put("idSession", idSession);
			contentValues.put("idWorkout", idWorkout);
			contentValues.put("fecha", aux);
			contentValues.put("comentario", sb.toString());
			db.insert("Entrenamientos", null, contentValues);
		}
		else if (localName.equals("kg")) {
			aux = sb.toString();
			sb.setLength(0);
		}
		else if (localName.equals("ejercicio")) {
			contentValues.clear();
			contentValues.put("idExercise", idExercise);
			contentValues.put("idLoad", idLoad);
			contentValues.put("idSession", idSession);
			contentValues.put("kg", aux);
			contentValues.put("g", sb.toString());
			db.insert("Ejercicios", null, contentValues);
		}	}
	
	@Override
	public void endDocument() throws SAXException {
	}
}
