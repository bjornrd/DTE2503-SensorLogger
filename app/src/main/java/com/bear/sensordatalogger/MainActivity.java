package com.bear.sensordatalogger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private boolean _isRecording;
    private SensorLogger _logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _isRecording = false;

        Context context = getApplicationContext();
        _logger = new SensorLogger(context);

        Button button = (Button) findViewById(R.id.recordButton);
        button.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(_isRecording) // Stop Recording
            {
                button.setText(getString(R.string.start_recording));
                _isRecording = false;

                _logger.stopLogger();

            } else { // Start Recording
                button.setText(getString(R.string.stop_recording));
                _isRecording = true;

                _logger.startLogger(getStorageDir());
            }
        }
        });



    }

    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();
    }
}