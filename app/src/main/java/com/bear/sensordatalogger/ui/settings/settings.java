package com.bear.sensordatalogger.ui.settings;

import static com.bear.sensordatalogger.SensorLogger.*;

import android.annotation.SuppressLint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bear.sensordatalogger.R;
import com.bear.sensordatalogger.SensorLogger;
import com.bear.sensordatalogger.databinding.SettingsFragmentBinding;
import com.bear.sensordatalogger.ui.SettingsViewModel;

import java.util.EnumSet;

import kotlin.Function;

public class settings extends Fragment {

    private SettingsViewModel _viewModel;
    private SettingsFragmentBinding _binding;

    // To avoid if-else hyperinflation in switch-isChecked() clauses.
    private interface SwitchChecker<T extends Enum<T>>
    {
        EnumSet<T> getSwitchStates();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false);

        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        _viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        setUpListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    public void setUpListeners()
    {
        setUpSensorDelayListeners();
        setUpSensorTypeListeners();
        setUpSensorReportingModeListener();
        setUpSensorLowPowerModeListener();
    }

    private void setUpSensorDelayListeners() {
        RadioButton sdno_rad_button = (RadioButton) requireView().findViewById(R.id.sensorDelayNormalRadioButton);
        RadioButton sdui_rad_button = (RadioButton) requireView().findViewById(R.id.sensorDelayUIRadioButton);
        RadioButton sdga_rad_button = (RadioButton) requireView().findViewById(R.id.sensorDelayGameRadioButton);
        RadioButton sdfa_rad_button = (RadioButton) requireView().findViewById(R.id.sensorDelayFastestRadioButton);

        sdno_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_NORMAL));
        sdui_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_UI));
        sdga_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_GAME));
        sdfa_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_FASTEST));
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorTypeListeners()
    {
        Switch stba_switch = (Switch) requireView().findViewById(R.id.sensorTypeBaseSwitch);
        Switch stco_switch = (Switch) requireView().findViewById(R.id.sensorTypeCompoundSwitch);

        SwitchChecker<SensorType> switchChecker = () -> {
            EnumSet<SensorType> reportingMode = EnumSet.noneOf(SensorType.class);

            if(stba_switch.isChecked())
                reportingMode.add(SensorType.base);

            if(stco_switch.isChecked())
                reportingMode.add(SensorType.composite);

            return reportingMode;
        };

        stba_switch.setOnCheckedChangeListener((compoundButton, b) -> _viewModel.getSensorType().setValue(switchChecker.getSwitchStates()));
        stco_switch.setOnCheckedChangeListener((compoundButton, b) -> _viewModel.getSensorType().setValue(switchChecker.getSwitchStates()));
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorReportingModeListener()
    {
        Switch scor_switch = (Switch) requireView().findViewById(R.id.sensorReportingModeContinuousSwitch);
        Switch socr_switch = (Switch) requireView().findViewById(R.id.sensorReportingModeOnChangeSwitch);
        Switch sosr_switch = (Switch) requireView().findViewById(R.id.sensorReportingModeOneshotSwitch);
        Switch sspr_switch = (Switch) requireView().findViewById(R.id.sensorReportingModeSpecialSwitch);

        SwitchChecker<ReportingMode> switchChecker = () -> {
            EnumSet<ReportingMode> reportingMode = EnumSet.noneOf(ReportingMode.class);

            if(scor_switch.isChecked())
                reportingMode.add(ReportingMode.continuous);

            if(socr_switch.isChecked())
                reportingMode.add(ReportingMode.onChange);

            if(sosr_switch.isChecked())
                reportingMode.add(ReportingMode.oneShot);

            if(sspr_switch.isChecked())
                reportingMode.add(ReportingMode.special);

            return reportingMode;
        };

        scor_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(switchChecker.getSwitchStates()) );
        socr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(switchChecker.getSwitchStates()) );
        sosr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(switchChecker.getSwitchStates()) );
        sspr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(switchChecker.getSwitchStates()) );
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorLowPowerModeListener()
    {
        Switch lowPowerModeSwitch = (Switch) requireView().findViewById(R.id.lowPowerModeSwitch);

        lowPowerModeSwitch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getUseLowPowerMode().setValue(lowPowerModeSwitch.isChecked()));
    }



}