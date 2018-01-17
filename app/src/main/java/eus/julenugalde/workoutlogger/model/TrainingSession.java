package eus.julenugalde.workoutlogger.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Represents a training session, storing information about the date and the workout carried out
 */
public class TrainingSession implements Serializable {
	/**Max length of the session description*/
    public static final int MAX_LENGTH_COMMENT = 100;

	private static final long serialVersionUID = 3108842315284397781L;
	private Date date;
	private int idWorkout;
	private String nameWorkout; 	//Redundant, but makes easier to display the information
	private String comment;

    /** Constructor that initializes the training session information
     *
     * @param date Session date. Only the day/month/year part will be used
     * @param idWorkout Identifier of the {@link Workout} object associated to the session
     * @param nameWorkout Name of the associated workout
     * @param comment Short comment related to the training session (limit
     *                TrainingSession.MAX_LENGTH_COMMENT)
     */
    public TrainingSession(Date date, int idWorkout, String nameWorkout, String comment) {
        this.setDate(date);
        this.setIdWorkout(idWorkout);
        this.setNameWorkout(nameWorkout);
        this.setComment(comment);
    }

     /** Constructor that initializes the object with the current date and empty information */
	public TrainingSession() {
		this(new Date(), 0, "", "");
	}

    /** Sets the workout id
     *
     * @param id Workout identifier
     */
	public void setIdWorkout (int id) {
		this.idWorkout = id;
	}

    /** Returns the workout id
     *
     * @return Workout identifier
     */
	public int getIdWorkout () {
		return idWorkout;
	}

    /** Returns the workout date
     *
     * @return Workout date
     */
	public Date getDate() {
		return date;
	}

    /** Sets the workout date
     *
     * @param date Workout date
     */
	public void setDate(Date date) {
		this.date = date;
	}

    /** Returns the workout name
     *
     * @return Name of the workout carried out during the training session
     */
	public String getNameWorkout() {
		return nameWorkout;
	}

    /** Sets the workout name
     *
     * @param nameWorkout Name of the workout carried out during the training session
     */
	public void setNameWorkout(String nameWorkout) {
		this.nameWorkout = nameWorkout;
	}

    /** Returns the comment related to the training session
     *
     * @return Short comment about the training session (limit TrainingSession.MAX_LENGTH_COMMENT)
     */
	public String getComment() {
		return comment;
	}

    /** Registers a comment about the training session
     *
     * @param comment String limited to TrainingSession.MAX_LENGTH_COMMENT. If it's longer, the
     *                string will be trimmed.
     */
	public void setComment(String comment) {
		if (comment.length() > MAX_LENGTH_COMMENT)
			comment = comment.substring(0, MAX_LENGTH_COMMENT);
		this.comment = comment;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return "{\"name\":\"" + nameWorkout + "\", \"id\":" + idWorkout + ", \"date\":\"" +
				sdf.format(date) + "\", \"comment\":\"" + comment + "\"}";
	}
}
