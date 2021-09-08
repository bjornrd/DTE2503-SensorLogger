package com.bear.sensordatalogger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.EnumSet;

public class MainActivity extends AppCompatActivity {

    private SensorLogger _logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Context context = getApplicationContext();
        _logger = new SensorLogger(context);

        Button button = (Button) findViewById(R.id.recordButton);
        button.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(_logger.isRecording()) // Stop Recording
            {
                button.setText(getString(R.string.start_recording));

                _logger.stopLogger();

            } else { // Start Recording
                button.setText(getString(R.string.stop_recording));

                _logger.setSensorDelay(SensorManager.SENSOR_DELAY_NORMAL);
                _logger.startLogger(getStorageDir(), SensorLogger.SensorType.all, SensorLogger.ReportingMode.all);

//                _logger.setLowPowerMode(true);
//                _logger.setSensorDelay(SensorManager.SENSOR_DELAY_NORMAL);
//                _logger.startLogger(getStorageDir(),
//                                    EnumSet.of(SensorLogger.SensorType.base),
//                                    EnumSet.of(SensorLogger.ReportingMode.continuous, SensorLogger.ReportingMode.onChange));
            }
        }
        });



    }

    private String getStorageDir()
    {
        return this.getExternalFilesDir(null).getAbsolutePath();
    }
}