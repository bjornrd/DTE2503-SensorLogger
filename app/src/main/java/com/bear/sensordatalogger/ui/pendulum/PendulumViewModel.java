package com.bear.sensordatalogger.ui.pendulum;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PendulumViewModel extends ViewModel {
    private MutableLiveData<String> _text;

    public PendulumViewModel() {
        _text = new MutableLiveData<>();
        _text.setValue("This is log fragment");
    }

    public LiveData<String> getText() {
        return _text;
    }
    // TODO: Implement the ViewModel
}