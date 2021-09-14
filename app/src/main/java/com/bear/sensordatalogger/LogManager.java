package com.bear.sensordatalogger;

import android.hardware.SensorEvent;

// Intent: Ability to choose log-method -- File write, database ...

public interface LogManager {

    public abstract void write(String message);

    public abstract void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3);

    public abstract void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2);

    public abstract void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1);


    public abstract boolean isOpen();
    public abstract void close();
}



