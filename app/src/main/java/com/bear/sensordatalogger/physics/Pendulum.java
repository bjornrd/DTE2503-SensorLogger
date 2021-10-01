package com.bear.sensordatalogger.physics;

import java.util.ArrayList;
import java.util.List;

// Spherical Pendulum:
//  https://en.wikipedia.org/wiki/Spherical_pendulum

public class Pendulum {

    private Coordinate _hangPoint;
    private Coordinate _centerOfGravity;
    private double _radius;
    private double _lineLength;
    private double _mass;
    private boolean _isTethered;

    private double _theta; // radians
    private double _dampening;
    private double _velocity;

    private double _v0x;
    private double _v0y;
    private double _vx;
    private double _vy;

    public Pendulum(Coordinate pendulumCenter, Coordinate hangPoint, double lineLength, double radius, double mass, double dampening)
    {
        _centerOfGravity = pendulumCenter;
        _hangPoint = hangPoint;
        _radius = radius;
        _lineLength = lineLength;

        _mass = mass;
        _dampening = dampening;
        _velocity = 0.0;

        _isTethered = true; // Always start off tethered to line

        _theta = calculateTheta();

        _v0x = 0;
        _v0y = 0;
        _vx = 0;
        _vy = 0;
    }

    private double calculateTheta()
    {
        return Math.atan2((_centerOfGravity.x - _hangPoint.x), -(_centerOfGravity.y - _hangPoint.y));
    }

    private Force combineForces(List<Force> forces)
    {
        Force combinedForces = new Force(0, 0, 0, _mass, "");
        for(Force force : forces)
        {
            combinedForces.Fx += force.Fx;
            combinedForces.Fy += force.Fy;
            combinedForces.Fz += force.Fz;
        }

        return combinedForces;
    }

    public void calculateNextPosition(List<Force> forces, double dt)
    {
        if(_isTethered) {
            // Calculation from:
            // https://www.khanacademy.org/computing/computer-programming/programming-natural-simulations/programming-oscillations/a/trig-and-forces-the-pendulum
            Force force = combineForces(forces);

            // Angular acceleration
            double alpha = (-force.Fy * Math.sin(_theta) +
                    force.Fx * Math.cos(_theta)) / _lineLength * dt;

            _velocity += alpha;
            _velocity *= _dampening;
            _theta += _velocity;

            _centerOfGravity.x = _lineLength * Math.sin(_theta);
            _centerOfGravity.y = -_lineLength * Math.cos(_theta);

        } else {
            _vy += -0.5*(-9.81/*gravity*/)*dt;

            // Bluntly ignoring any air resistance in my phone
            _centerOfGravity.y += _vy;
            _centerOfGravity.x += _vx;

        }
    }

    public void setXPos(double x)
    {
        _centerOfGravity.x = x;
    }

    public void setYPos(double y)
    {
        _centerOfGravity.y = y;
    }

    public void setZPos(double z)
    {
        _centerOfGravity.z = z;
    }

    public Coordinate getPos()
    {
        return _centerOfGravity;
    }

    public Coordinate getHangPoint()
    {
        return _hangPoint;
    }

    public void setHangpoint(Coordinate hangPoint) {
        _hangPoint = hangPoint;
    }

    public double mass()
    {
        return _mass;
    }

    public void setMass(double newMass)
    {
        _mass = newMass;
    }

    public double radius()
    {
        return _radius;
    }

    public void setRadius(double radius)
    {
        _radius = radius;
    }

    public void setLineLength(double lineLength)
    {
        _lineLength = lineLength;
    }

    public double lineLength()
    {
        return _lineLength;
    }

    public void release()
    {
        _isTethered = false;

        _v0x = _velocity * _lineLength * Math.cos(_theta);
        _v0y = _velocity * _lineLength * Math.sin(_theta);

        _vx = _v0x;
        _vy = _v0y;
    }

    public boolean isTethered()
    {
        return _isTethered;
    }

    public void setDampening(double dampening)
    {
        _dampening = dampening;
    }

}

