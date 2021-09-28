package com.bear.sensordatalogger;

import android.hardware.SensorEvent;

// Faux writer used when sensor logging to file is off.

public class FauxWriter implements LogManager {

    public FauxWriter()
    {
    }

    @Override
    public void write(String message) {

    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3) {

    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2) {

    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1) {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() {

    }
}
