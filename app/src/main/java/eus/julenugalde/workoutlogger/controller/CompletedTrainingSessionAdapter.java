package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
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
    private TrainingSession trainingSession;
    private TextView lblName;
    private TextView lblDate;
    private TextView lblComment;

    public CompletedTrainingSessionAdapter(
            Activity context, ArrayList<TrainingSession> listTrainingSessions) {
        super(context, R.layout.listitem_completed_training_session, listTrainingSessions);
        this.context = context;
        this.listTrainingSessions = listTrainingSessions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View item = inflater.inflate(R.layout.listitem_completed_training_session, null);
        trainingSession = listTrainingSessions.get(position);

        lblName = (TextView)item.findViewById(R.id.LblListItemCompletedTrainingSessionName);
        lblName.setText(trainingSession.getNameWorkout());

        lblDate = (TextView)item.findViewById(R.id.LblListItemCompletedTrainingSessionDate);
        SimpleDateFormat sdf;
        if (getContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE){   //Landscape: compact date format
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else {    //Portrait: long date format
            sdf = new SimpleDateFormat(getContext().getString(R.string.readable_date_format));
        }
        lblDate.setText(sdf.format(trainingSession.getDate()));

        lblComment = (TextView)item.findViewById(R.id.LblListItemCompletedTrainingSessionComment);
        lblComment.setText(trainingSession.getComment());

        return item;
    }
}
