package com.eddieowens.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.eddieowens.RNBoundaryModule;
import com.eddieowens.errors.GeofenceErrorMessages;
import com.facebook.react.HeadlessJsTaskService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static com.eddieowens.RNBoundaryModule.TAG;

public class BoundaryEventJobIntentService extends JobIntentService {

    public static final String ON_ENTER = "onEnter";
    public static final String ON_EXIT = "onExit";

    public BoundaryEventJobIntentService() {
        super();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "Handling geofencing event");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        Log.i(TAG, "Geofence transition: " + geofencingEvent.getGeofenceTransition());
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error in handling geofence " + GeofenceErrorMessages.getErrorString(geofencingEvent.getErrorCode()));
            return;
        }
        switch (geofencingEvent.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "Enter geofence event detected. Sending event.");
                final ArrayList<String> enteredGeofences = new ArrayList<>();
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    enteredGeofences.add(geofence.getRequestId());
                }
                sendEvent(this.getApplicationContext(), ON_ENTER, enteredGeofences);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "Exit geofence event detected. Sending event.");
                final ArrayList<String> exitingGeofences = new ArrayList<>();
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    exitingGeofences.add(geofence.getRequestId());
                }
                sendEvent(this.getApplicationContext(), ON_EXIT, exitingGeofences);
                break;
        }
    }

    private void sendEvent(Context context, String event, ArrayList<String> params) {
        final Intent intent = new Intent(RNBoundaryModule.GEOFENCE_DATA_TO_EMIT);
        intent.putExtra("event", event);
        intent.putExtra("params", params);

        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        bundle.putStringArrayList("ids", intent.getStringArrayListExtra("params"));

        Intent headlessBoundaryIntent = new Intent(context, BoundaryEventHeadlessTaskService.class);
        headlessBoundaryIntent.putExtras(bundle);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isAppOnForeground(context)) {
            context.startService(headlessBoundaryIntent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
        }
        else {
            // Since Oreo (8.0) and up they have restricted starting background services, and it will crash the app
            // But we can create a foreground service and bring an notification to the front
            // http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
            context.startForegroundService(headlessBoundaryIntent);
        }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}