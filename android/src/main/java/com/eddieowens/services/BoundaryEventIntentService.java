package com.eddieowens.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    public void onReceive(final Context context, final Intent intent) {
        if (intent != null) {
            boolean wasLaunched = wakeUpAppIfNotRunning(context);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    intent.setAction(ACTION);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }, wasLaunched ? 2000 : 0);
        }
    }

    private Boolean isActivityRunning(Context context, Class activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    private Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean wakeUpAppIfNotRunning(Context context) {
        Class intentClass = getMainActivityClass(context);
        Boolean isRunning = isActivityRunning(context, intentClass);

        if (!isRunning) {
            Intent intent = new Intent(context, intentClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            context.startActivity(intent);
        }

        return !isRunning;
    }
}
