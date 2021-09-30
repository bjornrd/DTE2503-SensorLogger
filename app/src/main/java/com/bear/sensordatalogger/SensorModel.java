package com.bear.sensordatalogger;

import android.hardware.SensorEvent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SensorModel extends ViewModel {

    private final MutableLiveData<SensorEvent>
            _sensorEvent = new MutableLiveData<>();

    public MutableLiveData<SensorEvent> getSensorEvent() {
        return _sensorEvent;
    }
}
