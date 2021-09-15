package com.bear.sensordatalogger.ui.dashboard;

import androidx.lifecycle.ViewModelProvider;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bear.sensordatalogger.R;
import com.bear.sensordatalogger.SensorLogger;
import com.bear.sensordatalogger.SensorLoggerService;
import com.bear.sensordatalogger.databinding.DashboardFragmentBinding;

public class dashboard extends Fragment {

    private DashboardViewModel _viewModel;
    private DashboardFragmentBinding _binding;
    Intent _loggerIntent;

    Context _context;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _context = getActivity().getApplicationContext();
        _viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        _binding = DashboardFragmentBinding.inflate(inflater, container, false);



        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        _loggerIntent = new Intent(_context, SensorLoggerService.class);

        Button button = (Button) getView().findViewById(R.id.recordButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Upwards of 80+ lost frames on initial button click, so thread the logging.
                // Make sure that all UI-related stuff is run on UI thread.
                new Thread(() -> {
                    if(isMyServiceRunning(SensorLoggerService.class)) // Stop Recording
                    {
                        getActivity().runOnUiThread(() -> button.setText(getString(R.string.start_recording)));

                        _context.stopService(_loggerIntent);

                    } else { // Start Recording
                        getActivity().runOnUiThread(() -> button.setText(getString(R.string.stop_recording)));

                        _loggerIntent.putExtra("fileName", getStorageDir());
                        _loggerIntent.putExtra("delay", SensorManager.SENSOR_DELAY_NORMAL);
                        _loggerIntent.putExtra("sensorTypes",  SensorLogger.SensorType.all);
                        _loggerIntent.putExtra("reportingModes", SensorLogger.ReportingMode.all);
                        _loggerIntent.putExtra("lowPowerMode", Boolean.FALSE);

                        _context.startForegroundService(_loggerIntent);
                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
        _context.stopService(_loggerIntent);
    }

    private String getStorageDir()
    {
        return getActivity().getExternalFilesDir(null).getAbsolutePath();
    }

    // https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android/5921190#5921190
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}