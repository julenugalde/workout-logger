package eus.julenugalde.workoutlogger.view;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.util.Log;

import eus.julenugalde.workoutlogger.R;

public class ActivityPreferences extends PreferenceActivity {
    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


}
