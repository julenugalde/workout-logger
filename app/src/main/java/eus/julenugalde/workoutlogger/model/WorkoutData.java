package eus.julenugalde.workoutlogger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/** This interface manages the database transactions for the application. It provides methods to open
 * and close the database, load/store/delete workout information (including associated tracks and
 * loads), load/store/delete training sessions information (including training exercises).
 * It also provides methods to import/export information through the {@link Persistence} interface.
 */
public interface WorkoutData {

    /** Opens the database. In case of error, a message is logged
     *
     * @return true if successful (even if the SQLiteDatabase object retreived is null), false
     * if an SQLiteException occurs.
     */
    public boolean open();

    /** Closes the database */
    public void close();

    /** Deletes all the database records */
    public void cleanDB();

    /** Reads all the database contents and prints them in the log (Log.i() method) */
    public void readDB();

    /** Checks if the workout to be stored exists
     *
     * @param workoutName name of the workout to be stored
     * @return true if the workout exists
     */
    public boolean existsWorkout(String workoutName);

    /** Inserts a workout information into the database
     *
     * @param workout Workout to be stored in the database
     * @return true if successful, false otherwise
     */
    public boolean insertWorkout(Workout workout);

    /** Deletes a workout from the database. It also deletes all the training sessions associated
     * to the workout.
     *
     * @param nameWorkout Name of the workout to be deleted, e.g. "BodyPump 85"
     * @return true if it's correctly deleted; false if there's any error, the workout doesn't
     * exist or more than one workout is stored with the same name.
     */
    public boolean deleteWorkout(String nameWorkout);

    /** Retrieves the workout information based on the name
     *
     * @param nameWorkout Name of the workout carried out in the session
     * @return If found, workout information; null if not found or error
     */
    public Workout getWorkout(String nameWorkout);

    /** Recovers the list of workouts from the database
     *
     * @return Array list with all the workouts stored in the database
     */
    public ArrayList<Workout> getListWorkouts();

    /** Returns the number of stored training sessions for a given workout
     *
     * @param nameWorkout Name of the workout
     * @return Number of training sessions stored in the database
     */
    public int getNumTrainingSessions(String nameWorkout);

    /** Retrieves the training session information based on the workout name and the date
     *
     * @param nameWorkout Name of the workout carried out in the session
     * @param date Training session date
     * @return If found, training session information; null if not found
     */
    public Workout getTrainingSession(String nameWorkout, Date date); //TODO It should return a TrainingSession object

    /** Deletes a training session from the database
     *
     * @param trainingSession Training session to be deleted
     * @return true if the training session was found and successfully deleted; false if there's
     * any error or the session wasn't found in the database
     */
    public boolean deleteTrainingSession(TrainingSession trainingSession);

    /** Stores a training session in the database
     *
     * @param workout Workout associated to the training session
     * @param listExercises ArrayList with the exercises performed
     * @param date Date with format yyyy-MM-dd
     * @param comment String limited to TrainingSession.MAX_LENGTH_COMMENT
     * @return true if successfully stored; false if there's any error
     */
    public boolean insertTrainingSession (Workout workout, ArrayList<TrainingExercise> listExercises,
                                          String date, String comment);

    /** Returns the list of training sessions stored in the database
     *
     * @return Array list with all the training sessions stored in the database
     * @throws Exception
     */
    public ArrayList<TrainingSession> getListTrainingSessions() throws Exception;

    /** Returns the global index calculated as the average value of the workout loads'
     * weigth, in order to perform a global comparison between training sessions. If a track has
     * not been carried out during the session, it's not taken into account.
     *
     * @param nameWorkout	Name of the workout for which the indices will be calculated
     * @return	Map of dates and indices. null if the workout doesn't exists or there are errors
     */
    public Map<Date, Double> getIndicesWorkout(String nameWorkout);
}
