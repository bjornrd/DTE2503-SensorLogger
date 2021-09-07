package com.bear.sensordatalogger;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorLogger implements SensorEventListener2 {

    List<Sensor> _sensors;
    SensorManager _sensorManager;
    String _logFileName;
    FileWriter _logWriter;
    boolean _isRecording;
    Context _context;

    SensorLogger(Context context)
    {
        _context = context;
        _sensorManager = (SensorManager) _context.getSystemService(SENSOR_SERVICE);
        _sensors = _sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    public void startLogger(String logFileName)
    {
        _logFileName = logFileName;

        try {
            _logWriter = new FileWriter(new File(_logFileName, "sensor_log_" + System.currentTimeMillis() + ".csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerListeners();

        _isRecording = true;
    }

    public void stopLogger()
    {
        _sensorManager.flush(this);
        _sensorManager.unregisterListener(this);

        try {
            _logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _isRecording = false;
    }

    private void registerListeners()
    {
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), 0);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), 0);
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(_isRecording) {
            try {
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        _logWriter.write(String.format("%d; Accelerometer; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        _logWriter.write(String.format("%d; Ambient Temperature; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_GRAVITY:
                        _logWriter.write(String.format("%d; Gravity; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        _logWriter.write(String.format("%d; Gyroscope; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_LIGHT:
                        _logWriter.write(String.format("%d; Light Level; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        _logWriter.write(String.format("%d; Magnetic Field; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_MOTION_DETECT:
                        _logWriter.write(String.format("%d; Motion Detector; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_PRESSURE:
                        _logWriter.write(String.format("%d; Pressure; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        _logWriter.write(String.format("%d; Relative Humidity; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        _logWriter.write(String.format("%d; Proximity Sensor; %f; %f; %f; %f; %f; %f\n", sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f, 0.f, 0.f, 0.f));
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public List<String> listSensors()
    {
        List<String> sensorList = new ArrayList<>();

        for(Sensor sensor:_sensors)
            sensorList.add(sensor.toString());

        return sensorList;
    }
}
