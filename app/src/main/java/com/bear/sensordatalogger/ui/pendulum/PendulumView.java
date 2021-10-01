package com.bear.sensordatalogger.ui.pendulum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.bear.sensordatalogger.databinding.PendulumFragmentBinding;
import com.bear.sensordatalogger.physics.Coordinate;
import com.bear.sensordatalogger.physics.Force;
import com.bear.sensordatalogger.physics.Pendulum;

import java.util.ArrayList;
import java.util.List;

// Inspired by:
// https://github.com/enyason/Simple_Pendulum_Demonstration/blob/master/app/src/main/java/com/nexdev/enyason/simple_pendulum_demonstration/PendulumView.java

// TODO: Implement timer (or class) to control how often we refresh the pendulum position
//
public class PendulumView extends View {

    private PendulumViewModel _viewModel;
    private PendulumFragmentBinding _binding;
    private Pendulum _pendulum;
    private List<Force> _forces;

    Paint paintCircle, paintThread;
    Path pathThread,pathHolder;

    int pendulum_x;
    int pendulum_y;

    public PendulumView(Context context)
    {
        super(context);
        init();
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public PendulumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


        int xCenter = getWidth() / 2;
        int yCenter = getHeight() / 2;

        _pendulum.setHangpoint(new Coordinate(Coordinate.Dimension.TwoD,xCenter , yCenter ,0));
        _pendulum.setLineLength(Math.min(xCenter, yCenter)-50);

        float boundaryRight = xCenter + 200;
        float boundaryLeft = xCenter - 200;
        float boundaryBottom = yCenter - 200;

        Log.i("width ", String.valueOf(getWidth()));

        paintCircle = new Paint();
        paintThread = new Paint();

        pathThread = new Path();
        pathHolder = new Path();

        float threadNewLine = xCenter + (float)(_pendulum.getHangPoint().x);

        //build paint for the circle
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(Color.BLACK);

        //build paint for the thread
        paintThread.setColor(Color.BLACK);
        paintThread.setStyle(Paint.Style.STROKE);
        paintThread.setStrokeWidth(3);


        //build path for the thread
        pathThread.moveTo((int)(_pendulum.getHangPoint().x), (int)(_pendulum.getHangPoint().y));
        pathThread.lineTo((int)(_pendulum.getPos().x + _pendulum.getHangPoint().x) , (int)(_pendulum.getPos().y + _pendulum.getHangPoint().y));

        canvas.drawPath(pathThread, paintThread);
        canvas.drawPath(pathHolder, paintThread);

        canvas.drawCircle(pendulum_x, pendulum_y, (int)_pendulum.radius(), paintCircle);
        canvas.drawCircle(xCenter, yCenter, 5, paintCircle);

        _pendulum.calculateNextPosition(_forces, 0.1);
        Coordinate position = _pendulum.getPos();
        Coordinate hangPoint = _pendulum.getHangPoint();
        pendulum_x = (int)position.x + (int)hangPoint.x;
        pendulum_y = (int)position.y + (int)hangPoint.y;

        Log.i("Pendulum x-position: ", String.valueOf(pendulum_x));
        Log.i("Pendulum y-position: ", String.valueOf(pendulum_y));

        Log.i("thread Line ", String.valueOf(threadNewLine));

        for(Force force : _forces)
        {
            if(force.identifier.equals("SomethingElse"))
            {
                force.Fx *= 0.999;

                if(Math.abs(force.Fx) < 9.2 && _pendulum.isTethered())
                    _pendulum.release();

            }
        }



        invalidate();
    }

    public void init()
    {
        _pendulum = new Pendulum(new Coordinate(Coordinate.Dimension.TwoD, 980, 0.0),
                                 new Coordinate(Coordinate.Dimension.TwoD, (float)getWidth()/2.0, 0.0),
                                 300, 15, 1, 0.98);

        _forces = new ArrayList<>();
        _forces.add(new Force(0, -9.81, 0.0, _pendulum.mass(), "Gravity"));
        _forces.add(new Force(-9.81, 0, 0, _pendulum.mass(), "SomethingElse"));

        Coordinate position = _pendulum.getPos();

        pendulum_x = (int)position.x;
        pendulum_y = (int)position.y;
    }

    public void releasePendulum()
    {
        _pendulum.release();
    }
}
