package com.bear.sensordatalogger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.EnumSet;


// Service inspired by: https://code.tutsplus.com/tutorials/android-barometer-logger-acquiring-sensor-data--mobile-10558

public class SensorLoggerService extends Service {

    private SensorLogger _logger;


    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        // https://github.com/smartdevicelink/sdl_java_suite/issues/843#issuecomment-416168295
        // Run as foreground service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelId);
        notificationChannel.setSound(null, null);

        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new Notification.Builder(this, channelId)
                                                    .setContentTitle(getString(R.string.app_name))
                                                    .setContentText("Recording Sensor Data")
                                                    .build();

        startForeground(1337, notification);

        // Set up and start logger
        _logger = new SensorLogger(getApplicationContext());

        // Get inputs
        Bundle extras = intent.getExtras();

        String fileName = (String) extras.get("fileName");
        int sensorDelay = (int) extras.get("delay");
        EnumSet<SensorLogger.SensorType> sensorTypes = (EnumSet<SensorLogger.SensorType>) extras.get("sensorTypes");
        EnumSet<SensorLogger.ReportingMode> reportingModes = (EnumSet<SensorLogger.ReportingMode>) extras.get("reportingModes");
        Boolean lowPowerMode = (Boolean) extras.get("lowPowerMode");

        _logger.setSensorDelay(sensorDelay);

        if(lowPowerMode != null)
            _logger.setLowPowerMode(lowPowerMode);

        if(sensorTypes != null && reportingModes == null)
            _logger.startLogger(fileName, sensorTypes);

        else if(sensorTypes == null && reportingModes != null)
            _logger.startLogger(fileName, SensorLogger.SensorType.all, reportingModes);

        else if (sensorTypes == null)
            _logger.startLogger(fileName);

        else
            _logger.startLogger(fileName, sensorTypes, reportingModes);




        // "... .START_STICKY is used for services that are explicitly started and stopped as needed, ..."
        // https://developer.android.com/reference/android/app/Service
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _logger.stopLogger();
    }

}
