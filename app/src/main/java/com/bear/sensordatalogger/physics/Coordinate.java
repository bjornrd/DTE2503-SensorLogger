package com.bear.sensordatalogger.physics;

public class Coordinate {

    public double x;
    public double y;
    public double z;
    public Dimension dim;


    public enum Dimension {
        TwoD,
        ThreeD
    }

    public Coordinate() {
        init(Dimension.TwoD, 0.0, 0.0, 0.0);
    }

    public Coordinate(Dimension dim) {
        init(dim, 0.0, 0.0, 0.0);
    }

    public Coordinate(Dimension dim, double x) {
        init(dim, x, 0.0, 0.0);
    }

    public Coordinate(Dimension dim, double x, double y) {
        init(dim, x, y, 0.0);
    }

    public Coordinate(Dimension dim, double x, double y, double z) {
        init(dim, x, y, z);
    }

    private void init(Dimension dim, double x, double y, double z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
