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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet; // https://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/
import java.util.function.Function;


// Inspired by: https://github.com/ejoebstl/Android-Sensor-Log
//
// Docs:    https://source.android.com/devices/sensors/sensor-types
//          https://developer.android.com/reference/android/hardware/SensorEvent#values
//

public class SensorLogger implements SensorEventListener2 {

    Context                     _context;
    private final SensorManager _sensorManager;
    private LogManager          _logManager;
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
        _context = context;

        _sensorManager = (SensorManager) _context.getSystemService(SENSOR_SERVICE);
        _sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
        _lowPowerMode = false;
        _isRecording = false;
    }

    public void startLogger(String logFileName, EnumSet<SensorType> sensorType, EnumSet<ReportingMode> mode)
    {
        try {
            _logManager = new LogFileWriter(new File(logFileName, "sensor_log_" + System.currentTimeMillis() + ".csv"));

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
        _isRecording = false;

        _sensorManager.flush(this);
        _sensorManager.unregisterListener(this);
        _sensorManager.cancelTriggerSensor(_triggerEventListener, _sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION));

        _logManager.close();
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
        if(_logManager.isOpen() && _isRecording)
            {
                    @SuppressLint("SimpleDateFormat")
                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());

                    switch (sensorEvent.sensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            _logManager.writeSensorEvent(_context.getString(R.string.Accelerometer),               timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            _logManager.writeSensorEvent(_context.getString(R.string.Magnetic_Field),              timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GYROSCOPE:
                            _logManager.writeSensorEvent(_context.getString(R.string.Gyroscope),                   timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_PRESSURE:
                            _logManager.writeSensorEvent(_context.getString(R.string.Pressure),                    timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_AMBIENT_TEMPERATURE:
                            _logManager.writeSensorEvent(_context.getString(R.string.Ambient_Temperature),         timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_HEART_RATE:
                            _logManager.writeSensorEvent(_context.getString(R.string.Heart_Rate),                  timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_LIGHT:
                            _logManager.writeSensorEvent(_context.getString(R.string.Light_Level),                 timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_PROXIMITY:
                            _logManager.writeSensorEvent(_context.getString(R.string.Proximity_Distance),          timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_RELATIVE_HUMIDITY:
                            _logManager.writeSensorEvent(_context.getString(R.string.Relative_Humidity),           timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_LINEAR_ACCELERATION:
                            _logManager.writeSensorEvent(_context.getString(R.string.Linear_Acceleration),         timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_ROTATION_VECTOR:
                            _logManager.writeSensorEvent(_context.getString(R.string.Rotation_Vector),             timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GAME_ROTATION_VECTOR:
                            _logManager.writeSensorEvent(_context.getString(R.string.Game_Rotation_Vector),        timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GRAVITY:
                            _logManager.writeSensorEvent(_context.getString(R.string.Gravity),                     timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                            _logManager.writeSensorEvent(_context.getString(R.string.Geomagnetic_Rotation_Vector), timestamp, sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                            break;
                        case Sensor.TYPE_STEP_COUNTER:
                            _logManager.writeSensorEvent(_context.getString(R.string.Step_Counter),                timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_SIGNIFICANT_MOTION:
                            _logManager.writeSensorEvent(_context.getString(R.string.Significant_Motion),          timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case Sensor.TYPE_STEP_DETECTOR:
                            _logManager.writeSensorEvent(_context.getString(R.string.Step_Detector),               timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                        case TYPE_TILT_DETECTOR:
                            _logManager.writeSensorEvent(_context.getString(R.string.Tilt_Detector),               timestamp, sensorEvent, sensorEvent.values[0]);
                            break;
                    }

            }
    }

    @SuppressLint("DefaultLocale")
    private void writeTriggerEvent(String descriptor, String timestamp, TriggerEvent triggerEvent, float val1, float val2, float val3) throws IOException {
        if(_logManager.isOpen())
            _logManager.write(String.format("%s; %s; %d; %f; %f; %f\n", descriptor, timestamp, triggerEvent.timestamp, val1, val2, val3));

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

    // TODO: Move to LogFileWriter
    private class TriggerEventLoggerTask extends AsyncTask<TriggerEvent, Void, Void>
    {
        @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
        @Override
        protected Void doInBackground(TriggerEvent... triggerEvents) {
            TriggerEvent triggerEvent = triggerEvents[0];

            if(_logManager.isOpen() && _isRecording)
            {
                try
                {
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


