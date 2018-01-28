package eus.julenugalde.workoutlogger.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import eus.julenugalde.workoutlogger.R;
import eus.julenugalde.workoutlogger.model.TrainingSession;
import eus.julenugalde.workoutlogger.model.Workout;

/** Edit text that incorporates a character counter in order to avoid surpassing the
 * maximum length.
 */
@SuppressLint("AppCompatCustomView")
public class EditTextWithCounter extends EditText {
    /** Maximum text length allowed by the counter*/
    public static final int COUNT_LIMIT = TrainingSession.MAX_LENGTH_COMMENT;
    /** Text size in pixel units */
    public static final float TEXT_SIZE = 35;

    private Paint paintBackground;
    private Paint paintForeground;
    private float scale;

    public EditTextWithCounter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public EditTextWithCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EditTextWithCounter(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(getResources().getColor(R.color.colorPrimaryDark));
        paintBackground.setStyle(Paint.Style.FILL);

        paintForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintForeground.setColor(getResources().getColor(R.color.colorTextIcons));
        paintForeground.setTextSize(TEXT_SIZE);

        scale = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Paint the background with dark color
        canvas.drawRect(this.getWidth()-30*scale, 5*scale, this.getWidth()-(5*scale),
                20*scale, paintBackground);
        //Paint the number of characters
        canvas.drawText("" + (COUNT_LIMIT-this.getText().toString().length()),
                this.getWidth()-28*scale, 17*scale, paintForeground);
    }
}
