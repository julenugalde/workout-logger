package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.TrainingExercise;

public class TrainingExerciseAdapter extends ArrayAdapter<TrainingExercise> {
    Activity context;
    TrainingExercise trainingExercise;
    ArrayList<TrainingExercise> listTrainingExercises;
    TextView lblName;
    TextView lblLoads;

    public TrainingExerciseAdapter(Activity context, ArrayList<TrainingExercise> listTrainingExercises) {
        super(context, R.layout.listitem_training_exercise, listTrainingExercises);
        this.context = context;
        this.listTrainingExercises = listTrainingExercises;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View item = inflater.inflate(R.layout.listitem_training_exercise, null);
        trainingExercise = listTrainingExercises.get(position);

        //Capture the controls and fill them
        lblName = (TextView)item.findViewById(R.id.LblNewTrainingExerciseName);
        lblLoads = (TextView)item.findViewById(R.id.LblNewTrainingExerciseLoads);
        lblName.setText(trainingExercise.getName());
        updateControlsVisibility(item, trainingExercise);

        return item;
    }

    private void updateControlsVisibility(View item, TrainingExercise trainingExercise) {
        //Visibility depends upon whether the exercise was carried out
        if (lblLoads != null) {
            if (trainingExercise.isCompleted()) {   //Exercise carried out: put loads info
                lblName.setEnabled(true);
                StringBuffer sb = new StringBuffer();
                if (trainingExercise.getLoadName(0).isEmpty()) {    //No loads
                    sb.append(getContext().getResources().getString(R.string.new_training_session_no_loads));
                }
                else { //there are loads
                    for (int i=0; i<TrainingExercise.MAX_LOADS; i++) {
                        if (!trainingExercise.getLoadName(i).isEmpty()) {
                            sb.append(trainingExercise.getLoadName(i) + ": " +
                                    trainingExercise.getLoadKg(i) +
                                    getContext().getResources().getString(R.string.decimal_separator) +
                                    trainingExercise.getLoadG(i) + "kg.\n");
                        }
                    }
                }
                lblLoads.setText(sb.toString());
                lblLoads.setVisibility(View.VISIBLE);
            }
            else {  //Not carried out: set invisible
                lblName.setEnabled(false);
                lblLoads.setText("");
                lblLoads.setVisibility(View.INVISIBLE);
            }
        }
    }
}
