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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// Inspired by: https://github.com/ejoebstl/Android-Sensor-Log

public class SensorLogger implements SensorEventListener2 {

    SensorManager   _sensorManager;
    String          _logFileName;
    FileWriter      _logWriter;
    boolean         _isRecording;
    Context         _context;

    SensorLogger(Context context)
    {
        _context = context;
        _sensorManager = (SensorManager) _context.getSystemService(SENSOR_SERVICE);
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
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),       SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),             SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),           SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),               SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT),       SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),            SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),   SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),           SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),       SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(_isRecording) {
            try {

                String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        _logWriter.write(String.format("%s; %d; Accelerometer; %f; %f; %f\n",           timestamp, sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        _logWriter.write(String.format("%s; %d; Ambient Temperature; %f; %f; %f\n",     timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_GRAVITY:
                        _logWriter.write(String.format("%s; %d; Gravity; %f; %f; %f\n",                 timestamp, sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        _logWriter.write(String.format("%s; %d; Gyroscope; %f; %f; %f\n",               timestamp, sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                        break;
                    case Sensor.TYPE_LIGHT:
                        _logWriter.write(String.format("%s; %d; Light Level; %f; %f; %f\n",             timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        _logWriter.write(String.format("%s; %d; Magnetic Field; %f; %f; %f\n",          timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_MOTION_DETECT:
                        _logWriter.write(String.format("%s; %d; Motion Detector; %f; %f; %f\n",         timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_PRESSURE:
                        _logWriter.write(String.format("%s; %d; Pressure; %f; %f; %f\n",                timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        _logWriter.write(String.format("%s; %d; Relative Humidity; %f; %f; %f\n",       timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        _logWriter.write(String.format("%s; %d; Proximity Sensor; %f; %f; %f\n",        timestamp, sensorEvent.timestamp, sensorEvent.values[0], 0.f, 0.f));
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

    public boolean isRecording()
    {
        return _isRecording;
    }
}
