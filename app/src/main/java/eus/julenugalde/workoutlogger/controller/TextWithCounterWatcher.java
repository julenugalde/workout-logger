package eus.julenugalde.workoutlogger.controller;

import android.text.Editable;
import android.text.TextWatcher;

import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.view.EditTextWithCounter;

/** Listener class for {@link eus.julenugalde.workoutlogger.view.EditTextWithCounter} components
 * that controls that the text length doesn't surpass the count limit
 */
public class TextWithCounterWatcher implements TextWatcher {
    private EditTextWithCounter source;
    private int countLimit;

    public TextWithCounterWatcher (EditTextWithCounter editTextWithCounter) {
        source = editTextWithCounter;
        countLimit = editTextWithCounter.getCountLimit();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //Make sure that the text length limit is not surpassed
        if (charSequence.length() > countLimit) {
            source.setText(charSequence.subSequence(0, countLimit-1));
        }
    }

    @Override
    public void afterTextChanged(Editable editable) { }
}
