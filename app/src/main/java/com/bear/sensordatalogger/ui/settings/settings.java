package com.bear.sensordatalogger.ui.settings;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bear.sensordatalogger.databinding.SettingsFragmentBinding;

public class settings extends Fragment {

    private SettingsViewModel _viewModel;
    private SettingsFragmentBinding _binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        _binding = SettingsFragmentBinding.inflate(inflater, container, false);

        return _binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}