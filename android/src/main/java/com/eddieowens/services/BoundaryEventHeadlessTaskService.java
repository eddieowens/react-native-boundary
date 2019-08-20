package com.eddieowens.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.eddieowens.R;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class BoundaryEventHeadlessTaskService extends HeadlessJsTaskService {
    public static final String NOTIFICATION_CHANNEL_ID = "com.eddieowens.GEOFENCE_SERVICE_CHANNEL";

    @Nullable
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        return new HeadlessJsTaskConfig(
                "OnBoundaryEvent",
                extras != null ? Arguments.fromBundle(extras) : null,
                5000,
                true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startForegroundServiceNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        startForegroundServiceNotification();

        return result;
    }

    private void startForegroundServiceNotification() {
        Context context = this.getApplicationContext();

        // Channel for the foreground service notification
        createChannel(context);

        // Notification for the foreground service
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
                .setContentTitle("Geofence Service")
                .setContentText("You're close to the configured location.")
                .setOngoing(true)
                .setColor(ContextCompat.getColor(context, R.color.accent_material_light));
        Notification notification = builder.build();
        startForeground(999999999, notification);
        HeadlessJsTaskService.acquireWakeLockNow(context);
    }

    private void createChannel(Context context) {
        String NOTIFICATION_CHANNEL_NAME = "Geofence Service";
        String NOTIFICATION_CHANNEL_DESCRIPTION = "Only used to know when you're close to a configured location.";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}