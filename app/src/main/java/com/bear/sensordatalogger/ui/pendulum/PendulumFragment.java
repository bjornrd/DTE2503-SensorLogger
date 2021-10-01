package com.bear.sensordatalogger.ui.pendulum;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bear.sensordatalogger.databinding.PendulumFragmentBinding;

public class PendulumFragment extends Fragment {

    private PendulumViewModel _viewModel;
    private PendulumFragmentBinding _binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

//        _viewModel = new ViewModelProvider(this).get(PendulumViewModel.class);
//
        _binding = PendulumFragmentBinding.inflate(inflater, container, false);
//
////        final TextView textView = _binding.textLog;
////        _viewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
////            @Override
////            public void onChanged(@Nullable String s) {
////                textView.setText(s);
////            }
////        });
//
//        final View view = _binding.pendulumView;
//

        return _binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}