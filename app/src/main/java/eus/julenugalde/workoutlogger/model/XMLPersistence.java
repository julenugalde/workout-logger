package eus.julenugalde.workoutlogger.model;

import android.database.Cursor;
import android.util.Log;
import android.util.Xml;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/** Implementation of the {@link Persistence} interface that stores the information in an
 * XML file.
 */
public class XMLPersistence implements Persistence {
    private WorkoutData workoutData;

    private static final String TAG = XMLPersistence.class.getSimpleName();

    public XMLPersistence(WorkoutData workoutData) {
        this.workoutData = workoutData;
    }

    /** Cleans the database and loads the contents of an XML file into it
     *
     * @param inputStream Input stream from which the XML file will be read
     * @return true if the operation is successful; false if there are errors
     */
    @Override
    public boolean loadData(InputStream inputStream) {
        workoutData.open();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            WorkoutLoggerXMLHandler xmlHandler = new WorkoutLoggerXMLHandler(workoutData.db);
            reader.setContentHandler(xmlHandler);
            workoutData.cleanDB();
            reader.parse(new InputSource(inputStream));
            workoutData.close();
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Error loading XML: " + e.getLocalizedMessage());
            workoutData.close();
            return false;
        }
    }

    /** Writes the database contents into an XML file
     *
     * @param outputStream output stream where the XML file will be written
     * @return true if the operation is successful; false if there are errors
     */
    @Override
    public boolean saveData(OutputStream outputStream) {
        workoutData.open();
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(outputStream, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "info_workouts");

            //Save table 'Workouts'
            Cursor c = workoutData.db.rawQuery("SELECT * FROM Workouts", null);
            if (c.moveToFirst()) {
                serializer.startTag("", "workouts");
                do {
                    serializer.startTag("", "workout");
                    serializer.attribute("", "idWorkout", String.valueOf(c.getInt(0)));
                    serializer.startTag("", "nombreWorkout");
                    serializer.text(c.getString(1));
                    serializer.endTag("", "nombreWorkout");
                    serializer.endTag("", "workout");
                } while(c.moveToNext());
                serializer.endTag("", "workouts");
            }

            //Save table 'Tracks'
            c = workoutData.db.rawQuery("SELECT * FROM Tracks", null);
            if (c.moveToFirst()) {
                serializer.startTag("", "tracks");
                do {
                    serializer.startTag("", "track");
                    serializer.attribute("", "idTrack", String.valueOf(c.getInt(0)));
                    serializer.attribute("", "idWorkout", String.valueOf(c.getInt(1)));
                    serializer.startTag("", "nombreTrack");
                    serializer.text(c.getString(2));
                    serializer.endTag("", "nombreTrack");
                    serializer.endTag("", "track");
                } while(c.moveToNext());
                serializer.endTag("", "tracks");
            }

            //Save table 'Loads'
            c = workoutData.db.rawQuery("SELECT * FROM Loads", null);
            if (c.moveToFirst()) {
                serializer.startTag("", "loads");
                do {
                    serializer.startTag("", "load");
                    serializer.attribute("", "idLoad", String.valueOf(c.getInt(0)));
                    serializer.attribute("", "idTrack", String.valueOf(c.getInt(1)));
                    serializer.startTag("", "nombreLoad");
                    serializer.text(c.getString(2));
                    serializer.endTag("", "nombreLoad");
                    serializer.endTag("", "load");
                } while(c.moveToNext());
                serializer.endTag("", "loads");
            }

            //Save table 'Entrenamientos'
            c = workoutData.db.rawQuery("SELECT * FROM Entrenamientos", null);
            if (c.moveToFirst()) {
                serializer.startTag("", "entrenamientos");
                do {
                    serializer.startTag("", "entrenamiento");
                    serializer.attribute("", "idEntrenamiento", String.valueOf(c.getInt(0)));
                    serializer.attribute("", "idWorkout", String.valueOf(c.getInt(1)));
                    serializer.startTag("", "fecha");
                    serializer.text(c.getString(2));
                    serializer.endTag("", "fecha");
                    serializer.startTag("", "comentario");
                    serializer.text(c.getString(3));
                    serializer.endTag("", "comentario");
                    serializer.endTag("", "entrenamiento");
                } while(c.moveToNext());
                serializer.endTag("", "entrenamientos");
            }

            //Save table 'Ejercicios'
            c = workoutData.db.rawQuery("SELECT * FROM Ejercicios", null);
            if (c.moveToFirst()) {
                serializer.startTag("", "ejercicios");
                do {
                    serializer.startTag("", "ejercicio");
                    serializer.attribute("", "idEjercicio", String.valueOf(c.getInt(0)));
                    serializer.attribute("", "idLoad", String.valueOf(c.getInt(1)));
                    serializer.attribute("", "idEntrenamiento", String.valueOf(c.getInt(2)));
                    serializer.startTag("", "kg");
                    serializer.text(String.valueOf(c.getInt(3)));
                    serializer.endTag("", "kg");
                    serializer.startTag("", "g");
                    serializer.text(String.valueOf(c.getInt(4)));
                    serializer.endTag("", "g");
                    serializer.endTag("", "ejercicio");
                } while(c.moveToNext());
                serializer.endTag("", "ejercicios");
            }

            serializer.endTag("", "info_workouts");
            serializer.endDocument();
            c.close();
            workoutData.close();
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Error saving XML: " + e.getLocalizedMessage());
            workoutData.close();
            return false;
        }
    }
}
