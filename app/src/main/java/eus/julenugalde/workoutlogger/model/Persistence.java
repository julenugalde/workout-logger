package eus.julenugalde.workoutlogger.model;

import java.io.InputStream;
import java.io.OutputStream;

/** Interface that defines the methods for data persistence */
public interface Persistence {
    /** Load data from an {@link InputStream} to the application database. It will delete the
     * current stored information and replace it.
     *
     * @param inputStream Source of the information
     * @return <code>true</code> if the operation is successful; <code>false</code>
     * if there are errors
     */
    public boolean loadData(InputStream inputStream);

    /** Sava data from the application database to an {@link java.io.OutputStream}.
     *
     * @param outputStream Where the information will be written
     * @return <code>true</code> if the operation is successful; <code>false</code>
     * if there are errors
     */
    public boolean saveData(OutputStream outputStream);
}
