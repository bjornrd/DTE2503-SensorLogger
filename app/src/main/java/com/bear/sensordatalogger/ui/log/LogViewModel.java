package com.bear.sensordatalogger.ui.log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LogViewModel extends ViewModel {
    private MutableLiveData<String> _text;

    public LogViewModel() {
        _text = new MutableLiveData<>();
        _text.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return _text;
    }
    // TODO: Implement the ViewModel
}