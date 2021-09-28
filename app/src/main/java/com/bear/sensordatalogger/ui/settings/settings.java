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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.EnumSet;

import kotlin.Function;

public class settings extends Fragment {

    private SettingsViewModel _viewModel;
    private SettingsFragmentBinding _binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false);

        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        setUpListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    public Integer getSensorDelay() {
        RadioButton sdno_rad_button = requireView().findViewById(R.id.sensorDelayNormalRadioButton);
        RadioButton sdui_rad_button = requireView().findViewById(R.id.sensorDelayUIRadioButton);
        RadioButton sdga_rad_button = requireView().findViewById(R.id.sensorDelayGameRadioButton);
        RadioButton sdfa_rad_button = requireView().findViewById(R.id.sensorDelayFastestRadioButton);

        if (sdno_rad_button.isChecked())
            return SensorManager.SENSOR_DELAY_NORMAL;

        if (sdui_rad_button.isChecked())
            return SensorManager.SENSOR_DELAY_UI;

        if (sdga_rad_button.isChecked())
            return SensorManager.SENSOR_DELAY_GAME;

        if (sdfa_rad_button.isChecked())
            return SensorManager.SENSOR_DELAY_FASTEST;

        else
            return SensorManager.SENSOR_DELAY_NORMAL;
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public EnumSet<SensorType> getSensorType()
    {
        SwitchMaterial stba_switch = requireView().findViewById(R.id.sensorTypeBaseSwitch);
        SwitchMaterial stco_switch = requireView().findViewById(R.id.sensorTypeCompoundSwitch);

        EnumSet<SensorType> reportingMode = EnumSet.noneOf(SensorType.class);

        if(stba_switch.isChecked())
            reportingMode.add(SensorType.base);

        if(stco_switch.isChecked())
            reportingMode.add(SensorType.composite);

        return reportingMode;
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public EnumSet<ReportingMode> getReportingMode()
    {
        SwitchMaterial scor_switch = requireView().findViewById(R.id.sensorReportingModeContinuousSwitch);
        SwitchMaterial socr_switch = requireView().findViewById(R.id.sensorReportingModeOnChangeSwitch);
        SwitchMaterial sosr_switch = requireView().findViewById(R.id.sensorReportingModeOneshotSwitch);
        SwitchMaterial sspr_switch = requireView().findViewById(R.id.sensorReportingModeSpecialSwitch);

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
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Boolean getUseLowPowerMode()
    {
        SwitchMaterial lowPowerModeSwitch = requireView().findViewById(R.id.lowPowerModeSwitch);
        return lowPowerModeSwitch.isChecked();
    }


    public void setUpListeners()
    {
        setUpSensorDelayListeners();
        setUpSensorTypeListeners();
        setUpSensorReportingModeListener();
        setUpSensorLowPowerModeListener();
        setUpSensorLogToFileListener();
    }

    private void setUpSensorDelayListeners() {
        RadioButton sdno_rad_button = requireView().findViewById(R.id.sensorDelayNormalRadioButton);
        RadioButton sdui_rad_button = requireView().findViewById(R.id.sensorDelayUIRadioButton);
        RadioButton sdga_rad_button = requireView().findViewById(R.id.sensorDelayGameRadioButton);
        RadioButton sdfa_rad_button = requireView().findViewById(R.id.sensorDelayFastestRadioButton);

        sdno_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_NORMAL));
        sdui_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_UI));
        sdga_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_GAME));
        sdfa_rad_button.setOnClickListener(view -> _viewModel.getSensorDelay().setValue(SensorManager.SENSOR_DELAY_FASTEST));
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorTypeListeners()
    {
        SwitchMaterial stba_switch = requireView().findViewById(R.id.sensorTypeBaseSwitch);
        SwitchMaterial stco_switch = requireView().findViewById(R.id.sensorTypeCompoundSwitch);

        stba_switch.setOnCheckedChangeListener((compoundButton, b) -> _viewModel.getSensorType().setValue(getSensorType()));
        stco_switch.setOnCheckedChangeListener((compoundButton, b) -> _viewModel.getSensorType().setValue(getSensorType()));
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorReportingModeListener()
    {
        SwitchMaterial scor_switch = requireView().findViewById(R.id.sensorReportingModeContinuousSwitch);
        SwitchMaterial socr_switch = requireView().findViewById(R.id.sensorReportingModeOnChangeSwitch);
        SwitchMaterial sosr_switch = requireView().findViewById(R.id.sensorReportingModeOneshotSwitch);
        SwitchMaterial sspr_switch = requireView().findViewById(R.id.sensorReportingModeSpecialSwitch);

        scor_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(getReportingMode()) );
        socr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(getReportingMode()) );
        sosr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(getReportingMode()) );
        sspr_switch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getReportingMode().setValue(getReportingMode()) );
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setUpSensorLowPowerModeListener()
    {
        SwitchMaterial lowPowerModeSwitch = requireView().findViewById(R.id.lowPowerModeSwitch);

        lowPowerModeSwitch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getUseLowPowerMode().setValue(lowPowerModeSwitch.isChecked()));
    }

    private void setUpSensorLogToFileListener()
    {
        SwitchMaterial logToFileSwitch = requireView().findViewById(R.id.logToFileSwitch);

        logToFileSwitch.setOnCheckedChangeListener( (compoundButton, b) -> _viewModel.getLogToFile().setValue(logToFileSwitch.isChecked()));
    }



}