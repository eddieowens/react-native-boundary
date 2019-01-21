package com.eddieowens.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.eddieowens.R;
import com.eddieowens.RNBoundaryModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import android.util.Log;

import static com.eddieowens.RNBoundaryModule.ON_ENTER;
import static com.eddieowens.RNBoundaryModule.ON_EXIT;
import static com.eddieowens.RNBoundaryModule.TAG;

public class BoundaryEventIntentService extends BroadcastReceiver {

    private static final Logger logger = Logger.getLogger(TAG);

    public static final String ACTION = "RNBoundary.Event";

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.i(TAG, "onReceive");
        logger.info("Broadcasting event1");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("1", "blaname", importance);
                channel.setDescription("bla");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentTitle("NEw received")
                    .setContentText("RECEIVED NOT")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, mBuilder.build());


        if (intent != null) {

            logger.info("Broadcasting event");
            intent.setAction(ACTION);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
