package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.TrainingSession;

public class CompletedTrainingSessionAdapter extends ArrayAdapter<TrainingSession> {
    private Activity context;
    private ArrayList<TrainingSession> listTrainingSessions;

    public CompletedTrainingSessionAdapter(
            Activity context, ArrayList<TrainingSession> listTrainingSessions) {
        super(context, R.layout.listitem_completed_training_session, listTrainingSessions);
        this.context = context;
        this.listTrainingSessions = listTrainingSessions;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View item = inflater.inflate(R.layout.listitem_completed_training_session, null);
        TrainingSession trainingSession = listTrainingSessions.get(position);

        TextView lblName = item.findViewById(R.id.LblListItemCompletedTrainingSessionName);
        lblName.setText(trainingSession.getNameWorkout());

        TextView lblDate = item.findViewById(R.id.LblListItemCompletedTrainingSessionDate);
        if (getContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE){   //Landscape: compact date format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            lblDate.setText(sdf.format(trainingSession.getDate()));
        } else {    //Portrait: long date format
            lblDate.setText(LocalesManager.getLongDateString(trainingSession.getDate(),
                    getContext().getResources().getConfiguration().locale));

        }


        TextView lblComment = item.findViewById(R.id.LblListItemCompletedTrainingSessionComment);
        lblComment.setText(trainingSession.getComment());

        return item;
    }
}
