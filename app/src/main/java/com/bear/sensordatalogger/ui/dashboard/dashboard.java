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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class dashboard extends Fragment {

    private DashboardViewModel _viewModel;
    private DashboardFragmentBinding _binding;
    Intent _loggerIntent;

    Context _context;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _context = requireActivity().getApplicationContext();
        _viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        _binding = DashboardFragmentBinding.inflate(inflater, container, false);

        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        _loggerIntent = new Intent(_context, SensorLoggerService.class);

        Button button = (Button) requireView().findViewById(R.id.recordButton);

        if(isMyServiceRunning(SensorLoggerService.class))
            requireActivity().runOnUiThread(() -> button.setText(getString(R.string.stop_recording)));
        else
            requireActivity().runOnUiThread(() -> button.setText(getString(R.string.start_recording)));


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Upwards of 80+ lost frames on initial button click, so thread the logging.
                // Make sure that all UI-related stuff is run on UI thread.
                new Thread(() -> {

                    // Stop Recording
                    // -------------------------
                    if(isMyServiceRunning(SensorLoggerService.class))
                    {
                        requireActivity().runOnUiThread(() -> button.setText(getString(R.string.start_recording)));

                        _context.stopService(_loggerIntent);

                        // Enable the settings/log-menu items when stopping the logger
                        BottomNavigationView navView = (BottomNavigationView) requireActivity().findViewById(R.id.nav_view);

                        requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.settings_menu).setEnabled(true));
                        requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.log_menu).setEnabled(true));


                    // Start Recording
                    // -------------------------
                    } else {
                        requireActivity().runOnUiThread(() -> button.setText(getString(R.string.stop_recording)));

                        _loggerIntent.putExtra("fileName", getStorageDir());
                        _loggerIntent.putExtra("delay", SensorManager.SENSOR_DELAY_NORMAL);
                        _loggerIntent.putExtra("sensorTypes",  SensorLogger.SensorType.all);
                        _loggerIntent.putExtra("reportingModes", SensorLogger.ReportingMode.all);
                        _loggerIntent.putExtra("lowPowerMode", Boolean.FALSE);

                        _context.startForegroundService(_loggerIntent);

                        // Disable the settings/log-menu items when starting the logger --
                        // As we don't want the user to change settings mid-logging, thinking that
                        // the changes are applied on-the-fly (as they're not).
                        BottomNavigationView navView = (BottomNavigationView) requireActivity().findViewById(R.id.nav_view);

                        requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.settings_menu).setEnabled(false));
                        requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.log_menu).setEnabled(false));

                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }


    public void stopService()
    {
        if(_context != null)
            _context.stopService(_loggerIntent);
    }

    private String getStorageDir()
    {
        return requireActivity().getExternalFilesDir(null).getAbsolutePath();
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