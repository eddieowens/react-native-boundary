package com.eddieowens.services;

import android.app.Notification;
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

        if (Build.VERSION.SDK_INT >= 26) {
            String NOTIFICATION_CHANNEL_ID = "com.woffu.app.WOFFU_CHANNEL";
            String GROUP_KEY = "com.woffu.app.WOFFU_GROUP";

            Context context = this.getApplicationContext();

            // Notification for the foreground service
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setContentTitle("Woffu Geofencing Service")
                    .setContentText("Woffu is not spying you, or constantly tracking your location. This is a geofence, to know when you get closer to the work location.")
                    .setOngoing(true)
                    .setColor(ContextCompat.getColor(context, R.color.accent_material_light))
                    .setGroup(GROUP_KEY);
            Notification notification = builder.build();
            startForeground(9999999, notification);
        }
    }
}
