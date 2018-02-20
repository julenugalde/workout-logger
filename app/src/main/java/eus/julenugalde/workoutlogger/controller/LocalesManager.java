package eus.julenugalde.workoutlogger.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import eus.julenugalde.workoutlogger.R;

/** Helper class that provides static methods to get locale-based information
 */
public class LocalesManager {
    private static final String TAG = LocalesManager.class.getSimpleName();

    /** Returns a list of the locales defined in the application
     *
     * @return {@link String} array with the 2-character locale codes defined
     */
    public static String[] getLocales() {
        return getResources().getStringArray(R.array.localeCode);
    }

    /** Returns the default track names for every defined locales
     *
     * @return Bidimensional {@link String} array in which the rows represent the locales and the
     * columns the default tracks in the usual order.
     */
    public static String[][] getDefaultTrackNames() {
        Context context = WorkoutLoggerApp.getContext();
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        Resources resources; //= new Resources(context.getAssets(), new DisplayMetrics(), configuration);
        String[] locales = getLocales();
        String[][] defaultTrackNames = new String[locales.length][];
        Locale currentLocale = configuration.locale;    //Save the current locale
        for (int i=0; i<locales.length; i++) {
            configuration.setLocale(new Locale(locales[i]));
            resources = new Resources(context.getAssets(), new DisplayMetrics(), configuration);
            defaultTrackNames[i] = resources.getStringArray(R.array.defaultTracks);
        }
        configuration.setLocale(currentLocale); //Restore the current locale
        return defaultTrackNames;
    }

    public static String[] getDefaultTrackNamesForCurrentLocale() {
        return getResources().getStringArray(R.array.defaultTracks);
    }

    /** Returns the long date format depending on the locale */
    public static String getLongDateString(Date date, Locale locale) {
        String languageCode = locale.getLanguage();
        StringBuilder sb = new StringBuilder();
        String[] weekDays = getResources().getStringArray(R.array.weekdays);
        String[] months = getResources().getStringArray(R.array.months);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //Language codes are compared following the recommendation in
        // https://docs.oracle.com/javase/7/docs/api/java/util/Locale.html#getLanguage()
        if (languageCode.equals(new Locale("es").getLanguage())) {
            sb.append(weekDays[calendar.get(Calendar.DAY_OF_WEEK)-1] + ", ");
            sb.append(calendar.get(Calendar.DAY_OF_MONTH) + " de ");
            sb.append(months[calendar.get(Calendar.MONTH)] + " de ");
            sb.append(calendar.get(Calendar.YEAR));
            return sb.toString(); //"Lunes, 1 de enero de 1970";
        }
        else if (languageCode.equals(new Locale("eu").getLanguage())) {
            sb.append(weekDays[calendar.get(Calendar.DAY_OF_WEEK)-1] + ", ");
            sb.append(calendar.get(Calendar.YEAR) + "(e)ko ");
            sb.append(months[calendar.get(Calendar.MONTH)] + "ren ");
            sb.append(calendar.get(Calendar.DAY_OF_MONTH));
            return sb.toString(); //"Astelehena, 1970ko urtarrilaren 1";
        }
        else if (languageCode.equals(new Locale("en").getLanguage())) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy");
            return sdf.format(date);
        }
        else {
            Log.e(TAG, "Invalid locale: languageCode=" + languageCode);
            return "";
        }
    }

    private static Resources getResources() {
        Context context = WorkoutLoggerApp.getContext();
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        return new Resources(context.getAssets(), new DisplayMetrics(), configuration);
    }
}
