package eus.julenugalde.workoutlogger.model;

import java.io.Serializable;
import java.util.ArrayList;

/** Class to include the information of an exercise during a training session. */
public class TrainingExercise implements Serializable {
	/** Maximum number of loads to be used in a training session */
	public static final int MAX_LOADS = 4;

	private static final long serialVersionUID = 2425573992096267365L;
	private String name;
	private boolean flagCompleted;
	private ArrayList<Load> listLoads;

    /**Constructor that gets the name of the exercise and whether it was completed
     *
     * @param name Name of the training session exercise
     * @param completed Flag that indicates if the exercise was completed
     */
	public TrainingExercise(String name, boolean completed) {
		this.setName(name);
		this.setCompleted(completed);
		// An ArrayList is created with MAX_LOADS elements initialized to 0.
		listLoads = new ArrayList<Load>();
		for (int i=0; i<MAX_LOADS; i++) {
		    listLoads.add(new Load("", -1, 0, 0));
        }
    }

    /** Sets the exercise name
     *
     * @param exerciseName Training session exercise name
     */
	public void setName(String exerciseName) {
		this.name = exerciseName;
	}

    /** Returns the exercise name
     *
     * @return Training session exercise name
     */
	public String getName() {return name;}

    /** Indicates if the exercise, which was intended during the training session, was actually
     * carried out or not
     *
     * @param exerciseCompleted {@code true} if the exercise was completed
     */
	public void setCompleted(boolean exerciseCompleted) {
		this.flagCompleted = exerciseCompleted;
	}

    /** Indicates if the exercise, which was intended during the training session, was actually
     * carried out or not
     *
     * @return true if the exercise was completed
     */
	public boolean isCompleted() {return flagCompleted;}

    /** Sets the information on an specific load during an exercise
     *
     * @param loadNumber Order of the load during the exercise (not to be confused with
     *                   Load.idLoad), from 0 to TrainingExercise.MAX_LOADS
     * @param name Name of the load
     * @param kg Integer part of the load weight
     * @param g Fractional part of the load weight, rounded to the closest multiple of 250g.
     * @throws IndexOutOfBoundsException if loadNumber is out of bounds
     */
	public void setLoad(int loadNumber, String name, int kg, int g)
            throws IndexOutOfBoundsException{
	    if ((loadNumber<0) || (loadNumber>=MAX_LOADS)) {
            throw new IndexOutOfBoundsException(loadNumber + " is not a valid load number");
        }
        Load newLoad = new Load(name, -1, kg, g);
	    listLoads.set(loadNumber,newLoad);
    }

    /** Returns the name of a load used during an exercise
     *
     * @param loadNumber Order of the load during the exercise (not to be confused with
     *                   Load.idLoad), from 0 to TrainingExercise.MAX_LOADS-1
     * @return Name of the load
     * @throws IndexOutOfBoundsException if loadNumber is out of bounds
     */
    public String getLoadName(int loadNumber) throws IndexOutOfBoundsException {
        if ((loadNumber<0) || (loadNumber>=MAX_LOADS)) {
            throw new IndexOutOfBoundsException(loadNumber + " is not a valid load number");
        }
        return listLoads.get(loadNumber).getName();
    }

    /** Returns the integer part of a load's weight during an exercise
     *
     * @param loadNumber Order of the load during the exercise (not to be confused with
     *                   Load.idLoad), from 0 to TrainingExercise.MAX_LOADS
     * @return Integer part of the load's weight
     * @throws IndexOutOfBoundsException if loadNumber is out of bounds
     */
    public int getLoadKg(int loadNumber) throws IndexOutOfBoundsException {
        if ((loadNumber<0) || (loadNumber>=MAX_LOADS)) {
            throw new IndexOutOfBoundsException(loadNumber + " is not a valid load number");
        }
        return listLoads.get(loadNumber).getKg();
    }

    /** Returns the decimal part of a load's weight during an exercise
     *
     * @param loadNumber Order of the load during the exercise (not to be confused with
     *                   Load.idLoad), from 0 to TrainingExercise.MAX_LOADS
     * @return Decimal part of the load's weight
     * @throws IndexOutOfBoundsException if loadNumber is out of bounds
     */
    public int getLoadG(int loadNumber) throws IndexOutOfBoundsException {
        if ((loadNumber < 0) || (loadNumber >= MAX_LOADS)) {
            throw new IndexOutOfBoundsException(loadNumber + " is not a valid load number");
        }
        return listLoads.get(loadNumber).getG();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\"name\": \"" + getName() + "\", \"completed\":" +
                isCompleted() + ", \"loads\": [");
        for (int i=0; i<MAX_LOADS; i++) {
            sb.append(listLoads.get(i).toString() + ",");
        }
        //Replace the final comma with the array closing character
        sb.replace(sb.length()-1, sb.length(), "]}");
        return sb.toString();
    }
}
