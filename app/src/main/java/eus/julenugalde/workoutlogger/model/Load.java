package eus.julenugalde.workoutlogger.model;

import java.io.Serializable;

/** Class that represents a load inside a track */
public class Load implements Serializable {
	private static final long serialVersionUID = -2320678259743403515L;
	private String name;
	private int idLoad;
	private int kg;
	private int g;
	
	/** Max load to be registered */
	public final static int MAX_LOAD = 80;
	/** Max length of the load name */
	public final static int NAME_MAX_LENGTH = 30;

	/** Constructor with only the load name specified. Default values for the loads and id
     * @param name Name of the load
     */
	public Load (String name) {
		this (name, 0, 1, 250);
	}

    /** Constructor with all the load information
     *
     * @param name Name of the load
     * @param idLoad Unique identifier
     * @param kg Load value in kg.
     * @param g Decimal value of the load. Only multiples of 250g. are allowed
     */
	public Load (String name, int idLoad, int kg, int g) {
		if (name.length() > 30)
			name = name.substring(0, 29);
		this.name = name;
		if (kg < 1) kg = 1;
		if (kg >= MAX_LOAD) {
			kg = MAX_LOAD;
			g = 0;
		}
		this.kg = kg;
		this.g = g-(g%250);
		this.idLoad=idLoad;
	}

    /** Returns the id
     *
     * @return Unique id for the load
     */
	public int getIdLoad() {return this.idLoad;}

    /** Sets the id
     *
     * @param idLoad Unique id for the load
     */
	public void setIdLoad(int idLoad) {this.idLoad = idLoad;}

    /** Returns the decimal part of the load value
     *
     * @return Decimal part of the load value, in 250g multiples
     */
	public int getG() {
		return g;
	}

    /** Returns the integer part of the load value
     *
     * @return Integer part of the load value
     */
	public int getKg() {
		return kg;
	}

    /** Returns the load name
     *
     * @return Load name that will be used to identify it in the application
     */
	public String getName() {
		return name;
	}

    /** Sets the decimal part of the load, in 250g multiples
     *
     * @param g Decimal part of the load value. It will be rounded to the closest 250-multiple (floor)
     */
	public void setG(int g) {
		this.g = g-(g%250);	}

    /**	Sets the integer part of the load
     *
     * @param kg Integer part of the load, in the [1, <code>Load.MAX_LOAD</code>] range
     */
	public void setKg(int kg) {
		if (kg < 1) kg = 1;
		if (kg > MAX_LOAD) kg = MAX_LOAD;
		this.kg = kg;
	}

    /** Sets the name of the load
     *
     * @param name Load name, with <code>Load.NAME_MAX_LENGTH</code> characters at most
     */
	public void setName(String name) {
        if (name.length() > NAME_MAX_LENGTH)
            name = name.substring(0, NAME_MAX_LENGTH - 1);
        this.name = name;
    }

    @Override
	public String toString() {
		return "{\"name\":\"" + name + "\", \"id\": " + idLoad + ", \"value\": " + kg + "." + g + "}";
	}

	//TODO Test application
	public static void main(String[] args) {
	    Load test = new Load("testLoad");
	    test.setIdLoad(55);
	    test.setKg(10);
	    test.setG(265); //will be rounded to 250
        System.out.println(test.toString());
    }
}
