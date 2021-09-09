package com.bear.sensordatalogger;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.EnumSet;

public class MainActivity extends AppCompatActivity {

    Intent _loggerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _loggerIntent = new Intent(getApplicationContext(), SensorLoggerService.class);

        Button button = (Button) findViewById(R.id.recordButton);
        button.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(isMyServiceRunning(SensorLoggerService.class)) // Stop Recording
            {
                button.setText(getString(R.string.start_recording));
                stopService(_loggerIntent);

            } else { // Start Recording
                button.setText(getString(R.string.stop_recording));

                _loggerIntent.putExtra("fileName", getStorageDir());
                _loggerIntent.putExtra("delay", SensorManager.SENSOR_DELAY_NORMAL);
                _loggerIntent.putExtra("sensorTypes",  SensorLogger.SensorType.all);
                _loggerIntent.putExtra("reportingModes", SensorLogger.ReportingMode.all);

                startForegroundService(_loggerIntent);
            }
        }
        });

    }

    @Override
    protected void onDestroy()
    {
        stopService(_loggerIntent);
        super.onDestroy();
    }

    private String getStorageDir()
    {
        return this.getExternalFilesDir(null).getAbsolutePath();
    }

    // https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android/5921190#5921190
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}