package eus.julenugalde.workoutlogger.model;

import android.content.Context;

/** Factory class to create instances of {@link eus.julenugalde.workoutlogger.model.WorkoutData} */
public class WorkoutDataFactory {
    public static WorkoutData getInstance(Context context) {
        //TODO Add option to preferences window to select {"firebase", "sqlite"}
        String databaseType = "sqlite";

        if (databaseType.equals("firebase")) {
            return new WorkoutDataFirebase();
        }
        else if (databaseType.equals("sqlite")) {
            return new WorkoutDataSQLite(context);
        }
        else {
            return null;
        }
    }
}
