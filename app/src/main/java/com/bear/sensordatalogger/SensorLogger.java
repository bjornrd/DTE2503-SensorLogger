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
import java.util.Date;
import java.util.EnumSet; // https://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/


// Inspired by: https://github.com/ejoebstl/Android-Sensor-Log
//

// Docs: https://source.android.com/devices/sensors/sensor-types

public class SensorLogger implements SensorEventListener2 {

    SensorManager   _sensorManager;
    FileWriter      _logWriter;
    boolean         _isRecording;
    int             _sensorDelay;
    boolean         _lowPowerMode;
    final int       TYPE_TILT_DETECTOR = 22;  // https://android.googlesource.com/platform/cts/+/master/tests/sensor/src/android/hardware/cts/SensorSupportTest.java : l127

    enum ReportingMode
    {
        onChange,
        continuous,
        oneShot,
        special;

        public static final EnumSet<ReportingMode> all = EnumSet.allOf(ReportingMode.class);
    }

    enum SensorType
    {
        base,
        composite;
        public static final EnumSet<SensorType> all = EnumSet.allOf(SensorType.class);
    }

    SensorLogger(Context context)
    {
        _sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        _sensorDelay = SensorManager.SENSOR_DELAY_UI;
        _lowPowerMode = false;
    }

    public void startLogger(String logFileName, EnumSet<SensorType> sensorType, EnumSet<ReportingMode> mode)
    {
        try {
            _logWriter = new FileWriter(new File(logFileName, "sensor_log_" + System.currentTimeMillis() + ".csv"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sensorType.contains(SensorType.base))
            registerBaseSensors(mode);

        if (sensorType.contains(SensorType.composite))
            registerCompositeSensors(mode);

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

    private void registerBaseSensors(EnumSet<ReportingMode> mode)
    {
        if (mode.contains(ReportingMode.continuous))
            registerContinuousBaseSensors();

        if (mode.contains(ReportingMode.onChange))
            registerOnChangeBaseSensors();
    }


    private void registerContinuousBaseSensors()
    {
        registerListener(Sensor.TYPE_ACCELEROMETER);                // Non-wake-up -- Low power
        registerListener(Sensor.TYPE_MAGNETIC_FIELD);               // Non-wake-up -- Low power
        if(!_lowPowerMode) registerListener(Sensor.TYPE_GYROSCOPE); // Non-wake-up
        registerListener(Sensor.TYPE_PRESSURE);                     // Non-wake-up
    }

    private void registerOnChangeBaseSensors()
    {
        registerListener(Sensor.TYPE_AMBIENT_TEMPERATURE);  // Non-wake-up
        registerListener(Sensor.TYPE_HEART_RATE);           // Non-wake-up
        registerListener(Sensor.TYPE_LIGHT);                // Non-wake-up
        registerListener(Sensor.TYPE_PROXIMITY);            // Wake-up
        registerListener(Sensor.TYPE_RELATIVE_HUMIDITY);    // Non-wake-up
    }

    private void registerCompositeSensors(EnumSet<ReportingMode> modes)
    {
        if (modes.contains(ReportingMode.onChange))
            registerOnChangeCompositeSensors();

        if (modes.contains(ReportingMode.continuous))
            registerContinuousCompositeSensors();

        if (modes.contains(ReportingMode.oneShot))
            registerOneShotCompositeSensors();

        if (modes.contains(ReportingMode.special))
            registerSpecialCompositeSensors();
    }

    private void registerContinuousCompositeSensors()
    {
        if(!_lowPowerMode) registerListener(Sensor.TYPE_LINEAR_ACCELERATION);   // Non-wake-up
        if(!_lowPowerMode) registerListener(Sensor.TYPE_ROTATION_VECTOR);       // Non-wake-up
        if(!_lowPowerMode) registerListener(Sensor.TYPE_GAME_ROTATION_VECTOR);  // Non-wake-up
        if(!_lowPowerMode) registerListener(Sensor.TYPE_GRAVITY);               // Non-wake-up
        registerListener(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);              // Non-wake-up

    }

    private void registerOnChangeCompositeSensors()
    {
        registerListener(Sensor.TYPE_STEP_COUNTER); // Non-wake-up -- Low Power
    }

    private void registerOneShotCompositeSensors()
    {
        registerListener(Sensor.TYPE_SIGNIFICANT_MOTION); // Wake-up -- Low Power
    }

    private void registerSpecialCompositeSensors()
    {
        registerListener(Sensor.TYPE_STEP_DETECTOR);    // Non-wake-up  -- Low Power
        registerListener(TYPE_TILT_DETECTOR);           // Wake-up      -- Low Power
    }

    private boolean registerListener(int sensorType)
    {
        if(hasSensorType(sensorType))
            return _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(sensorType), _sensorDelay);
        else
            return false;
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
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
                    case Sensor.TYPE_ROTATION_VECTOR:
                        _logWriter.write(String.format("%s; %d; Rotation Sensor; %f; %f; %f\n",         timestamp, sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));

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

    public boolean lowPowerMode()
    {
        return _lowPowerMode;
    }

    public void setLowPowerMode(boolean lowPowerMode)
    {
        _lowPowerMode = lowPowerMode;
    }

    // https://android.googlesource.com/platform/cts/+/master/tests/sensor/src/android/hardware/cts/SensorSupportTest.java
    private boolean hasSensorType(int sensorType)
    {
        return (_sensorManager != null && _sensorManager.getDefaultSensor(sensorType) != null);
    }

    public int sensorDelay()
    {
        return _sensorDelay;
    }

    public void setSensorDelay(int sensorDelay)
    {
        switch (sensorDelay)
        {
            case SensorManager.SENSOR_DELAY_FASTEST:
                _sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                break;

            case SensorManager.SENSOR_DELAY_GAME:
                _sensorDelay = SensorManager.SENSOR_DELAY_GAME;
                break;

            case SensorManager.SENSOR_DELAY_UI:
                _sensorDelay = SensorManager.SENSOR_DELAY_UI;
                break;

            case SensorManager.SENSOR_DELAY_NORMAL:
                _sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                break;
        }
    }
}
