package com.bear.sensordatalogger;

import android.annotation.SuppressLint;
import android.hardware.SensorEvent;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFileWriter extends FileWriter implements LogManager
{
    private boolean _isOpen;


    private static class WriteParams
    {
        String descriptor;
        String timeStamp;
        SensorEvent sensorEvent;
        float val1;
        float val2;
        float val3;

        WriteParams(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3)
        {
            this.descriptor = descriptor;
            this.timeStamp = timeStamp;
            this.sensorEvent = sensorEvent;
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
        }
    }


    public LogFileWriter(File file) throws IOException {
        super(file);
        _isOpen = true;
    }

    public boolean isOpen()
    {
        return _isOpen;
    }

    @Override
    public void write(String msg)
    {
        try {
            super.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            super.close();
            _isOpen = false;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3) {
        internalWrite(descriptor, timeStamp, sensorEvent, val1, val2, val3);
    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2) {
        writeSensorEvent(descriptor, timeStamp, sensorEvent, val1, val2, 0.f);
    }

    @Override
    public void writeSensorEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1) {
        writeSensorEvent(descriptor, timeStamp, sensorEvent, val1, 0.f, 0.f);
    }

    @SuppressLint("DefaultLocale")
    private void writeEvent(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3)
    {
        this.write(String.format("%s; %s; %d; %f; %f; %f\n", descriptor, timeStamp, sensorEvent.timestamp, val1, val2, val3));
    }

    private void internalWrite(String descriptor, String timeStamp, SensorEvent sensorEvent, float val1, float val2, float val3)
    {
        // Do I need to make sure that asynctask's don't try to write to the same file at the same time?
        // Write operations should not take that long, so using SERIAL_EXECUTOR should not cause
        // a large backlog of stuff?...
        //
        // As stated in the doc for SERIAL_EXECUTOR:
        // "This field was deprecated in API level 30. Globally serializing tasks results in
        // excessive queuing for unrelated operations."
        new SensorEventLoggerTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new WriteParams(descriptor, timeStamp, sensorEvent, val1, val2, val3));
    }

    // This stuff seems to be deprecated in API level 30 -- and they recommend using
    // Executors instead.  That'll be for another implementation, another time.
    @SuppressLint("StaticFieldLeak")
    private class SensorEventLoggerTask extends AsyncTask<WriteParams, Void, Void>
    {
        @Override
        protected Void doInBackground(WriteParams... writeParams)
        {
            String descriptor       = writeParams[0].descriptor;
            String timeStamp        = writeParams[0].timeStamp;
            SensorEvent sensorEvent = writeParams[0].sensorEvent;
            float val1              = writeParams[0].val1;
            float val2              = writeParams[0].val2;
            float val3              = writeParams[0].val3;

            writeEvent(descriptor, timeStamp, sensorEvent, val1, val2, val3);

            return null;
        }
    }
}
