package com.bear.sensordatalogger.physics;

import java.util.ArrayList;
import java.util.List;

// Spherical Pendulum:
//  https://en.wikipedia.org/wiki/Spherical_pendulum

public class Pendulum {



    private Coordinate _hangPoint;
    private Coordinate _centerOfGravity;
    double _radius;
    double _lineLength;
    double _mass;

    double _theta; // radians
    double _dampening;
    double _velocity;

    public Pendulum(Coordinate pendulumCenter, Coordinate hangPoint, double lineLength, double radius, double mass, double dampening)
    {
        _centerOfGravity = pendulumCenter;
        _hangPoint = hangPoint;
        _radius = radius;
        _lineLength = lineLength;

        _mass = mass;
        _dampening = dampening;
        _velocity = 0.0;

        _theta = calculateTheta();
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
        // Calculation from:
        // https://www.khanacademy.org/computing/computer-programming/programming-natural-simulations/programming-oscillations/a/trig-and-forces-the-pendulum
        Force force = combineForces(forces);

        // Angular acceleration
        double alpha =( -force.Fy * Math.sin(_theta) +
                        force.Fx * Math.cos(_theta) )/_lineLength*dt ;

        _velocity += alpha;
        _velocity *= _dampening;
        _theta += _velocity;

        _centerOfGravity.x = _lineLength*Math.sin(_theta);
        _centerOfGravity.y = -_lineLength*Math.cos(_theta);

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

}

