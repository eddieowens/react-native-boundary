package com.eddieowens.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.eddieowens.RNBoundaryModule;
import com.eddieowens.errors.GeofenceErrorMessages;
import com.facebook.react.HeadlessJsTaskService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

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
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    Log.i(TAG, "Enter geofence event detected. Sending event.");
                    sendEvent(this.getApplicationContext(), ON_ENTER, geofence.getRequestId());
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    Log.i(TAG, "Exit geofence event detected. Sending event.");
                    sendEvent(this.getApplicationContext(), ON_EXIT, geofence.getRequestId());
                }
                break;
        }
    }

    private void sendEvent(Context context, String event, String id) {
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        bundle.putString("id", id);

        Intent headlessBoundaryIntent = new Intent(context, BoundaryEventHeadlessTaskService.class);
        headlessBoundaryIntent.putExtras(bundle);

        context.startService(headlessBoundaryIntent);
        HeadlessJsTaskService.acquireWakeLockNow(context);
    }
}