package com.bear.sensordatalogger;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bear.sensordatalogger.databinding.ActivityMainBinding;
import com.bear.sensordatalogger.ui.dashboard.dashboard;
import com.bear.sensordatalogger.ui.log.log;
import com.bear.sensordatalogger.ui.settings.settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{

    private ActivityMainBinding _binding;

    dashboard dashboardFragment = new dashboard();
    log logFragment = new log();
    settings settingsFragment = new settings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnItemSelectedListener(this);
        navView.setSelectedItemId(R.id.dashboard_menu);
    }

    // https://www.geeksforgeeks.org/bottom-navigation-bar-in-android/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemID = item.getItemId();

        // Using if-else instead of switch
        // https://stackoverflow.com/a/64335551
        if (itemID == R.id.dashboard_menu) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, dashboardFragment).commit();
            return true;
        }
        else if(itemID == R.id.log_menu)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, logFragment).commit();
            return true;
        }
        else if(itemID == R.id.settings_menu)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
            return true;
        }
        else
            return false;
    }

    @Override
    public void onDestroy()
    {
        dashboardFragment.stopService();
        super.onDestroy();
    }


}