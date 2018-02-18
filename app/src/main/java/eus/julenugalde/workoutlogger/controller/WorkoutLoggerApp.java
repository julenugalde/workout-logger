package eus.julenugalde.workoutlogger.controller;

import android.app.Application;
import android.content.Context;

/** This class simply extends {@link Application} and provides a static method to retrieve the
 * application {@link Context} from any class.
 */
public class WorkoutLoggerApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
