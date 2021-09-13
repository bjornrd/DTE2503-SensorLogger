package com.bear.sensordatalogger;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet; // https://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/
import java.util.concurrent.atomic.AtomicBoolean;


// Inspired by: https://github.com/ejoebstl/Android-Sensor-Log
//
// Docs:    https://source.android.com/devices/sensors/sensor-types
//          https://developer.android.com/reference/android/hardware/SensorEvent#values
//

public class SensorLogger implements SensorEventListener2 {
    private AtomicBoolean _writerIsOpen; // To assure that the _logWriter is open when executing write-AsyncTask - Pretty hacky solution...

    private final SensorManager _sensorManager;
    private FileWriter          _logWriter;
    private boolean             _isRecording;
    private int                 _sensorDelay;
    private boolean             _lowPowerMode;
    private final int           TYPE_TILT_DETECTOR = 22;  // https://android.googlesource.com/platform/cts/+/master/tests/sensor/src/android/hardware/cts/SensorSupportTest.java : l127
    private TriggerEventListener _triggerEventListener;

    public enum ReportingMode
    {
        onChange,
        continuous,
        oneShot,
        special;

        public static final EnumSet<ReportingMode> all = EnumSet.allOf(ReportingMode.class);
    }

    public enum SensorType
    {
        base,
        composite;
        public static final EnumSet<SensorType> all = EnumSet.allOf(SensorType.class);
    }

    SensorLogger(Context context)
    {
        _sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        _sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
        _lowPowerMode = false;
        _writerIsOpen = new AtomicBoolean(false);
    }

    public void startLogger(String logFileName, EnumSet<SensorType> sensorType, EnumSet<ReportingMode> mode)
    {
        try {
            _logWriter = new FileWriter(new File(logFileName, "sensor_log_" + System.currentTimeMillis() + ".csv"));
            _writerIsOpen.set(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sensorType.contains(SensorType.base))
            registerBaseSensors(mode);

        if (sensorType.contains(SensorType.composite))
            registerCompositeSensors(mode);

        _triggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent triggerEvent) {
                   new TriggerEventLoggerTask().execute(triggerEvent);
            }
        };

        _isRecording = true;
    }

    public void startLogger(String logFileName, EnumSet<SensorType> sensorType)
    {
        startLogger(logFileName, sensorType, ReportingMode.all);
    }

    public void startLogger(String logFileName)
    {
        startLogger(logFileName, SensorType.all, ReportingMode.all);
    }

    public void stopLogger()
    {
        _sensorManager.flush(this);
        _sensorManager.unregisterListener(this);
        _sensorManager.cancelTriggerSensor(_triggerEventListener, _sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION));

        try {
            _writerIsOpen.set(false);
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
        if(hasSensorType(Sensor.TYPE_SIGNIFICANT_MOTION))
            _sensorManager.requestTriggerSensor(_triggerEventListener, _sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)); // Wake-up -- Low Power
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        new SensorEventLoggerTask().execute(sensorEvent);
    }

    @SuppressLint("DefaultLocale")
    private void writeSensorEvent(String descriptor, String timestamp, SensorEvent sensorEvent, float val1, float val2, float val3) throws IOException
    {
        if(_writerIsOpen.get())
            _logWriter.write(String.format("%s; %s; %d; %f; %f; %f\n", descriptor, timestamp, sensorEvent.timestamp, val1, val2, val3));
    }

    private void writeSensorEvent(String descriptor, String timestamp, SensorEvent sensorEvent, float val1, float val2) throws IOException
    {
        writeSensorEvent(descriptor, timestamp, sensorEvent, val1, val2, 0.f);
    }

    private void writeSensorEvent(String descriptor, String timestamp, SensorEvent sensorEvent, float val1) throws IOException
    {
        writeSensorEvent(descriptor, timestamp, sensorEvent, val1, 0.f, 0.f);
    }

    @SuppressLint("DefaultLocale")
    private void writeTriggerEvent(String descriptor, String timestamp, TriggerEvent triggerEvent, float val1, float val2, float val3) throws IOException {
        if(_writerIsOpen.get())
            _logWriter.write(String.format("%s; %s; %d; %f; %f; %f\n", descriptor, timestamp, triggerEvent.timestamp, val1, val2, val3));

        // Re-enable the one-shot sensor that reported
        if(hasSensorType(triggerEvent.sensor.getType()))
            _sensorManager.requestTriggerSensor(_triggerEventListener, _sensorManager.getDefaultSensor(triggerEvent.sensor.getType())); // Wake-up -- Low Power
    }

    private void writeTriggerEvent(String descriptor, String timestamp, TriggerEvent triggerEvent, float val1) throws IOException
    {
        writeTriggerEvent(descriptor, timestamp, triggerEvent, val1, 0.f, 0.f);
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

    public void setSensorDelay_ms(int sensorDelay_ms)
    {
        if(sensorDelay_ms > 0)
            _sensorDelay = sensorDelay_ms*1000;
    }

    // Ignoring warning: https://stackoverflow.com/a/46166223
    private class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void>
    {

        @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
        @Override
        protected Void doInBackground(SensorEvent... sensorEvents) {
            SensorEvent sensorEvent = sensorEvents[0];

            if(_isRecording) {
                try {

                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
                    switch (sensorEvent.sensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            writeSensorEvent("Accelerometer",               timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            writeSensorEvent("Magnetic Field",              timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GYROSCOPE:
                            writeSensorEvent("Gyroscope",                   timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_PRESSURE:
                            writeSensorEvent("Pressure",                    timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_AMBIENT_TEMPERATURE:
                            writeSensorEvent("Ambient Temperature",         timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_HEART_RATE:
                            writeSensorEvent("Heart Rate",                  timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_LIGHT:
                            writeSensorEvent("Light Level",                 timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_PROXIMITY:
                            writeSensorEvent("Proximity Distance",          timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_RELATIVE_HUMIDITY:
                            writeSensorEvent("Relative Humidity",           timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_LINEAR_ACCELERATION:
                            writeSensorEvent("Linear Acceleration",         timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_ROTATION_VECTOR:
                            writeSensorEvent("Rotation Vector",             timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GAME_ROTATION_VECTOR:
                            writeSensorEvent("Game Rotation Vector",        timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GRAVITY:
                            writeSensorEvent("Gravity",                     timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                            writeSensorEvent("Geomagnetic Rotation Vector", timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_STEP_COUNTER:
                            writeSensorEvent("Step Counter",                timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_SIGNIFICANT_MOTION:
                            writeSensorEvent("Significant Motion",          timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_STEP_DETECTOR:
                            writeSensorEvent("Step Detector",               timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case TYPE_TILT_DETECTOR:
                            writeSensorEvent("Tilt Detector",               timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }
    }

    private class TriggerEventLoggerTask extends AsyncTask<TriggerEvent, Void, Void>
    {
        @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
        @Override
        protected Void doInBackground(TriggerEvent... triggerEvents) {
            TriggerEvent triggerEvent = triggerEvents[0];

            if(_isRecording) {
                try {

                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
                    switch (triggerEvent.sensor.getType()) {
                        case Sensor.TYPE_SIGNIFICANT_MOTION:
                            writeTriggerEvent("Significant Motion", timestamp, triggerEvent, triggerEvent.values[0]);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }
    }
}


