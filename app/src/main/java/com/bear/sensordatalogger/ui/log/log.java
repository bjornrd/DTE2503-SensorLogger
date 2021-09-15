package com.bear.sensordatalogger.ui.log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bear.sensordatalogger.R;
import com.bear.sensordatalogger.databinding.DashboardFragmentBinding;
import com.bear.sensordatalogger.databinding.LogFragmentBinding;

public class log extends Fragment {

    private LogViewModel _viewModel;
    private LogFragmentBinding _binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _viewModel = new ViewModelProvider(this).get(LogViewModel.class);

        _binding = LogFragmentBinding.inflate(inflater, container, false);

        final TextView textView = _binding.textLog;
        _viewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return _binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}