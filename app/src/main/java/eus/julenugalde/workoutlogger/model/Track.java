package eus.julenugalde.workoutlogger.model;

import java.io.Serializable;
import java.util.ArrayList;

/** Class that represents a track during a workout */
public class Track implements Serializable {
	/** Max length of the track name */
	public static final int NAME_MAX_LENGTH = 30;

    private static final long serialVersionUID = 6009538054128418205L;
	private String name;
	private ArrayList<Load> listLoads;

    /**Constructor that takes a track name and initializes the load list as an empty ArrayList
     *
     * @param name Track name
     */
	public Track (String name) {
		this.name = name;
		this.listLoads = new ArrayList<Load> ();
	}

    /**Default constructor that sets the name as an empty String */
	public Track() {
		this("");
	}

    /**Returns the track name
     *
     * @return Track name
     */
	public String getName() {
		return name;
	}
	
	/** Returns the list of track loads as an array
     *
     * @return Array of {@link Load} objects, null if there are not loads in the track
     */
	public Load[] getLoads() {
		if (listLoads.size()==0) return (Load[])null;
		return (Load[])listLoads.toArray();
		/*Load[] result = new Load[listLoads.size()];
		for (int i = 0; i< listLoads.size(); i++)
			result[i] = listLoads.get(i);
		return result;*/
	}

    /** Adds a new load to the track
     *
     * @param load Load to be added
     * @return <code>true</code> it the loads list changed
     */
	public boolean addLoad(Load load) {
		return listLoads.add(load);
	}

    /**Removes an element from the loads list
     *
     * @param index Index of the {@link Load} list to be removed
     * @return {@link Load} that was removed if the operation was successfull
     * @throws IndexOutOfBoundsException if the index is not valid
     */
	public Load removeLoad (int index) throws IndexOutOfBoundsException {
		return (Load)listLoads.remove(index);
	}

    /** Sets the track name
     *
     * @param newName track name, with <code>Load.NAME_MAX_LENGTH</code> characters at most
     */
	public void setName(String newName) {
		if (newName.length()>NAME_MAX_LENGTH)
			newName = newName.substring(0, NAME_MAX_LENGTH-1);
		name = newName;
	}

    /** Gets the number of loads in the track
     *
     * @return Number of {@link Load} objects in the track
     */
	public int getNumLoads() {
		return listLoads.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{\"name\": \"" + name + "\"");
	    if (listLoads.size() > 0) {
	        sb.append(", \"loads\": [");
            for (int i=0; i<listLoads.size(); i++) {
                sb.append(listLoads.get(i) + ",");
            }
            //Replace the final comma with the array closing character
            sb.replace(sb.length()-1, sb.length(), "]");
        }
		sb.append("}");
		return sb.toString();
	}

	//TODO test class
    public static void main(String[] args) {
	    Track track = new Track("testTrack");
	    track.addLoad(new Load("load1", 1, 20, 500));
	    track.addLoad(new Load("load2", 2, 12, 0));
	    track.addLoad(new Load("load3", 3, 5, 355));    //rounded to 250
        System.out.println(track.toString());
    }
}