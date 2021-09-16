package com.bear.sensordatalogger.ui;

import static com.bear.sensordatalogger.SensorLogger.*;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bear.sensordatalogger.SensorLogger;
import com.bear.sensordatalogger.SensorLogger.ReportingMode;

import java.util.EnumSet;

public class SettingsViewModel extends ViewModel{

    private final MutableLiveData<Integer>
            _sensorDelay    = new MutableLiveData<>();

    private final MutableLiveData<EnumSet<SensorType>>
            _sensorType     = new MutableLiveData<>();

    private final MutableLiveData<EnumSet<ReportingMode>>
            _reportingMode  = new MutableLiveData<>();

    private final MutableLiveData<Boolean>
            _useLowPowerMode = new MutableLiveData<>();


    public MutableLiveData<Integer> getSensorDelay()
    {
        return _sensorDelay;
    }

    public MutableLiveData<EnumSet<SensorType>> getSensorType()
    {
        return _sensorType;
    }

    public MutableLiveData<EnumSet<ReportingMode>> getReportingMode()
    {
        return _reportingMode;
    }

    public MutableLiveData<Boolean> getUseLowPowerMode()
    {
        return _useLowPowerMode;
    }

}
