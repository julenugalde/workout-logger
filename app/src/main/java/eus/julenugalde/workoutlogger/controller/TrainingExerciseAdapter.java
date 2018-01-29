package eus.julenugalde.workoutlogger.controller;

import android.app.Activity;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
    ImageView imgIcon;

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
        lblName = (TextView)item.findViewById(R.id.LblListItemTrainingExerciseName);
        lblLoads = (TextView)item.findViewById(R.id.LblListItemTrainingExerciseLoads);
        lblName.setText(trainingExercise.getName());
        imgIcon = (ImageView)item.findViewById(R.id.ImgListItemTrainingExerciseIcon);
        //TODO Replace strings by elements in a resource file
        String text = lblName.getText().toString();
        if (text.equals("Calentamiento"))
            imgIcon.setImageResource(R.drawable.track_warmup);
        else if (text.equals("Squad"))
            imgIcon.setImageResource(R.drawable.track_squats);
        else if (text.equals("Pectoral"))
            imgIcon.setImageResource(R.drawable.track_chest);
        else if (text.equals("Espalda"))
            imgIcon.setImageResource(R.drawable.track_back);
        else if (text.equals("Triceps"))
            imgIcon.setImageResource(R.drawable.track_triceps);
        else if (text.equals("Biceps"))
            imgIcon.setImageResource(R.drawable.track_biceps);
        else if (text.equals("Hombro"))
            imgIcon.setImageResource(R.drawable.track_shoulders);
        else if (text.equals("Lunge"))
            imgIcon.setImageResource(R.drawable.track_lunges);
        else if (text.equals("Abdominal"))
            imgIcon.setImageResource(R.drawable.track_core);
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
                                    trainingExercise.getLoadG(i) +
                                    getContext().getResources().getString(R.string.weight_unit) +
                                    "\n");
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
