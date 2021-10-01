package com.bear.sensordatalogger.physics;

public class Force {
    public double Fx;
    public double Fy;
    public double Fz;
    public String identifier;

    public Force(double ax, double ay, double az, double mass, String identifier)
    {
        Fx = ax * mass;
        Fy = ay * mass;
        Fz = az * mass;
        this.identifier = identifier;
    }
}
