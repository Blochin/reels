package sk.fei.stuba.bakalarskaPraca.animation;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.exoplayer2.Player;

public class ProgressBarAnimation  extends Animation {
    private Context context;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    private TextView textView;
    private float from;
    private  float to;
    private int state = 0;

    public ProgressBarAnimation(Context context, ProgressBar progressBar, TextView textView, float from, float to , ConstraintLayout constraintLayout ) {
        this.context = context;
        this.progressBar = progressBar;
        this.textView = textView;
        this.from = from;
        this.to = to;
        this.constraintLayout = constraintLayout;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from+(to-from)*interpolatedTime;
        if(state == Player.STATE_READY){
            this.setDuration(1);
            this.constraintLayout.setBackground(null);
            this.constraintLayout.setBackgroundColor(Color.BLACK);
            this.progressBar.setVisibility(View.INVISIBLE);
            this.textView.setVisibility(View.INVISIBLE);
        }else{
            progressBar.setProgress((int)(value));
            textView.setText((int)value+" %");
        }

        if(value == 100){
            this.constraintLayout.setBackground(null);
            this.constraintLayout.setBackgroundColor(Color.BLACK);
            this.progressBar.setVisibility(View.INVISIBLE);
            this.textView.setVisibility(View.INVISIBLE);
        }
    }


}
