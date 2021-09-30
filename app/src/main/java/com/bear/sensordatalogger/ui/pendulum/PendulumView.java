package com.bear.sensordatalogger.ui.pendulum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bear.sensordatalogger.databinding.PendulumFragmentBinding;

// Inspired by:
// https://github.com/enyason/Simple_Pendulum_Demonstration/blob/master/app/src/main/java/com/nexdev/enyason/simple_pendulum_demonstration/PendulumView.java


public class PendulumView extends View {

    private PendulumViewModel _viewModel;
    private PendulumFragmentBinding _binding;
    private Canvas _canvas;

    Paint paintCircle, paintThread;
    Path pathThread,pathHolder;

    float circlePositionX = 0;
    float threadPositionX = 0;

    int x_dir;
    int y_dir;

    double thread_x_dir;
    int circle_x;
    double thread_x;

    public PendulumView(Context context)
    {
        super(context);
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int xCenter = getWidth() / 2;
        int yCenter = getWidth() / 2;

        float boundaryRight = xCenter + 200;
        float boundaryLeft = xCenter - 200;
        float boundaryBottom = yCenter - 200;

        Log.i("width ", String.valueOf(getWidth()));

        paintCircle = new Paint();
        paintThread = new Paint();

        pathThread = new Path();
        pathHolder = new Path();

        float threadNewLine = xCenter + threadPositionX;


        //build paint for the circle
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(Color.BLACK);
//        paintCircle.setStrokeWidth(5);


        //build paint for the thread
        paintThread.setColor(Color.BLACK);
        paintThread.setStyle(Paint.Style.STROKE);
        paintThread.setStrokeWidth(3);


        pathHolder.moveTo(xCenter,100);
        pathHolder.lineTo(300,100);

        pathHolder.moveTo(300,100);
        pathHolder.lineTo(300,50);

        pathHolder.moveTo(300,50);
        pathHolder.lineTo(420,50);

        pathHolder.moveTo(420,50);
        pathHolder.lineTo(420,100);

        pathHolder.moveTo(420,100);
        pathHolder.lineTo(xCenter,100);

        //build path for the thread
        pathThread.moveTo(xCenter, 100);
        pathThread.lineTo((float) thread_x, 350);


        canvas.drawPath(pathThread, paintThread);
        canvas.drawPath(pathHolder,paintThread);

        canvas.drawCircle(circle_x, 370, 30, paintCircle);
        canvas.drawCircle(xCenter, 100, 5, paintCircle);


        if (circle_x >= boundaryRight) {
            x_dir -= 5;
        }


        if (circle_x <= boundaryLeft) {
            x_dir += 5;
        }

        circle_x = circle_x + x_dir;
        thread_x = thread_x + x_dir;

        Log.i("thread Line ", String.valueOf(threadNewLine));

        invalidate();
    }

    public void init()
    {
        x_dir = 5;
        thread_x_dir = 9.5;

        circle_x = 360;
        thread_x = 360;
    }


    private class Pendulum
    {
        public float circlePositionX = 0;
        float threadPositionX = 0;

        int x_dir;
        int y_dir;

        double thread_x_dir;
        int circle_x;
        double thread_x;
    }


}
