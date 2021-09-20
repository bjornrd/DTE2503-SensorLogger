package com.bear.sensordatalogger.ui.dashboard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bear.sensordatalogger.R;
import com.bear.sensordatalogger.SensorLogger;
import com.bear.sensordatalogger.SensorLogger.ReportingMode;
import com.bear.sensordatalogger.SensorLogger.SensorType;
import com.bear.sensordatalogger.SensorLoggerService;
import com.bear.sensordatalogger.databinding.DashboardFragmentBinding;
import com.bear.sensordatalogger.ui.SettingsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.EnumSet;

public class dashboard extends Fragment {

    private SettingsViewModel _viewModel;
    private DashboardFragmentBinding _binding;
    Intent _loggerIntent;

    Context _context;

    // Settings
    Integer                 _sensorDelay;
    EnumSet<SensorType>     _sensorType;
    EnumSet<ReportingMode>  _reportingMode;
    Boolean                 _lowPowerMode;

    boolean _defaultsHaveBeenSet = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _context = requireActivity().getApplicationContext();
        _binding = DashboardFragmentBinding.inflate(inflater, container, false);

        _viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        // Set up settings observers
        // -----------------------------------------------------------------------------------------
        final Observer<Integer> sensorDelayObserver =
                integer -> _sensorDelay = _viewModel.getSensorDelay().getValue();

        final Observer<EnumSet<SensorType>> sensorTypeObserver =
                enumSet -> _sensorType = _viewModel.getSensorType().getValue();

        final Observer<EnumSet<ReportingMode>> sensorReportingModeObserver =
                enumSet -> _reportingMode = _viewModel.getReportingMode().getValue();

        final Observer<Boolean> sensorLowPowerModeObserver =
                bool -> _lowPowerMode = _viewModel.getUseLowPowerMode().getValue();

        _viewModel.getSensorDelay()    .observe(requireActivity(), sensorDelayObserver);
        _viewModel.getSensorType()     .observe(requireActivity(), sensorTypeObserver);
        _viewModel.getReportingMode()  .observe(requireActivity(), sensorReportingModeObserver);
        _viewModel.getUseLowPowerMode().observe(requireActivity(), sensorLowPowerModeObserver);


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


        if(!_defaultsHaveBeenSet)
            setDefaultValues();

        button.setOnClickListener(view1 -> {

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

                    _loggerIntent.putExtra("fileName",          getStorageDir());
                    _loggerIntent.putExtra("delay",             _sensorDelay);
                    _loggerIntent.putExtra("sensorTypes",       _sensorType);
                    _loggerIntent.putExtra("reportingModes",    _reportingMode);
                    _loggerIntent.putExtra("lowPowerMode",      _lowPowerMode);

                    _context.startForegroundService(_loggerIntent);

                    // Disable the settings/log-menu items when starting the logger --
                    // As we don't want the user to change settings mid-logging, thinking that
                    // the changes are applied on-the-fly (as they're not).
                    BottomNavigationView navView = (BottomNavigationView) requireActivity().findViewById(R.id.nav_view);

                    requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.settings_menu).setEnabled(false));
                    requireActivity().runOnUiThread(() -> navView.getMenu().findItem(R.id.log_menu).setEnabled(false));

                }
            }).start();
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
        String filesDir = requireActivity().getFilesDir().getAbsolutePath();
        return requireActivity().getFilesDir().getAbsolutePath();
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

    public void setDefaultValues()
    {
        // Sensor Delay
        _sensorDelay = _context.getResources().getInteger(R.integer.sensor_delay_default_value);


        // Sensor Types
        EnumSet<SensorType> sensorTypes = EnumSet.noneOf(SensorType.class);

        int base = _context.getResources().getInteger(R.integer.sensor_type_base_default_value);
        int comp = _context.getResources().getInteger(R.integer.sensor_type_compound_default_value);

        if(base == 1)
            sensorTypes.add(SensorType.base);
        if(comp == 1)
            sensorTypes.add(SensorType.composite);

        _sensorType = sensorTypes;


        // Sensor Reporting Modes
        EnumSet<ReportingMode> reportingMode = EnumSet.noneOf(ReportingMode.class);

        int continuous  = _context.getResources().getInteger(R.integer.sensor_reporting_continuous_default_value);
        int onChange    = _context.getResources().getInteger(R.integer.sensor_reporting_onChange_default_value);
        int oneShot     = _context.getResources().getInteger(R.integer.sensor_reporting_oneShot_default_value);
        int special     = _context.getResources().getInteger(R.integer.sensor_reporting_special_default_value);


        if(continuous == 1)
            reportingMode.add(ReportingMode.continuous);
        if(onChange == 1)
            reportingMode.add(ReportingMode.onChange);
        if(oneShot == 1)
            reportingMode.add(ReportingMode.oneShot);
        if(special == 1)
            reportingMode.add(ReportingMode.special);

        _reportingMode = reportingMode;


        // Low Power Mode
        int defaultLowPowerMode = _context.getResources().getInteger(R.integer.sensor_low_power_mode_default_value);
        _lowPowerMode = defaultLowPowerMode == 1;

        _defaultsHaveBeenSet = true;
    }


}