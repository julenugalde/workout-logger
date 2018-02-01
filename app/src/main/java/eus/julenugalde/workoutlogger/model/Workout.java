package eus.julenugalde.workoutlogger.model;

import java.io.Serializable;
import java.util.ArrayList;

/**Class that represents a workout design. It will consist on a number of individual exercises
 * (tracks), each of them with or without loads (0..N)
 */
public class Workout implements Serializable {
    /** Max length of the workout name */
    public static final int NAME_MAX_LENGTH = 30;

    private static final long serialVersionUID = 7213290710726185589L;
	private String name;
	private int numTrainingSessions;
	private ArrayList<Track> listTracks;

    /**Constructor that takes a workout name and initializes the track list as an empty
     * {@link ArrayList}
     *
     * @param name Workout name
     */
    public Workout (String name) {
		this.name = name;
		this.listTracks = new ArrayList<Track>();
		this.numTrainingSessions = 0;
	}

    /**Default constructor that sets the name as an empty String */
    public Workout() {
		this("");
	}

    /** Returns the workout name
     *
     * @return Workout name
     */
	public String getName() {
		return name;
	}

    /** Adds a new track to the workout
     *
     * @param track Track to be added
     * @return <code>true</code> it the tracks list changed
     */
    public void addTrack(Track track) {
		listTracks.add(track);
	}

    /** Returns a specific Track from the workout
     *
     * @param index Track index within the workout
     * @return element at the specific location
     * @throws IndexOutOfBoundsException if the index is not valid
     */
	public Track getTrack(int index) throws IndexOutOfBoundsException {
		return listTracks.get(index);
	}

    /**Removes an element from the tracks list
     *
     * @param index Index of the {@link Track} list to be removed
     * @return {@link Track} that was removed if the operation was successful
     * @throws IndexOutOfBoundsException if the index is not valid
     */
	public Track removeTrack(int index) throws IndexOutOfBoundsException {
		return (Track)listTracks.remove(index);
	}

    /** Sets the workout name
     *
     * @param newName workout name, with <code>Workout.NAME_MAX_LENGTH</code> characters at most
     */
	public void setName(String newName) {
		if (newName.length()>NAME_MAX_LENGTH)
			newName = newName.substring(0, NAME_MAX_LENGTH-1);
		name = newName;
	}

    /** Returns the list of tracks as an array
     *
     * @return Array of {@link Track} objects, null if there are not tracks in the workout
     */
	public Track[] getTracks() {
		if (listTracks.size()==0) return (Track[])null;
        //return (Track[]) listTracks.toArray();
		Track[] result = new Track[listTracks.size()];
		for (int i = 0; i< listTracks.size(); i++)
			result[i] = listTracks.get(i);
		return result;
	}

    /** Returns the number of tracks in the workout
     *
     * @return Number of {@link Track} objects in the track
     */
	public int getNumTracks() {
		return listTracks.size();
	}

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder("{\n\"name\": \"" + name + "\"");
        if (listTracks.size() > 0) {
            sb.append(",\n\"tracks\": [");
            for (int i=0; i<listTracks.size(); i++) {
                sb.append(listTracks.get(i) + ",");
            }
            //Replace the final comma with the array closing character
            sb.replace(sb.length()-1, sb.length(), "]");
        }
        sb.append("\n}");
        return sb.toString();
    }

    /** Returns the total number of training sessions associated to the workout
     *
     * @return number of {@link TrainingSession} objects associated to the workout
     */
    public int getNumTrainingSessions() {
        return numTrainingSessions;
    }

    /** Sets the number of training sessions associated to the workout
     *
     * @param numTrainingSessions number of training sessions
     */
    public void setNumTrainingSessions(int numTrainingSessions) {
        this.numTrainingSessions = numTrainingSessions;
    }
}
