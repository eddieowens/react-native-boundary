package com.eddieowens.services;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eddieowens.RNBoundaryModule;
import com.eddieowens.errors.GeofenceErrorMessages;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
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

        Log.e(TAG, "GEOFENCING: " + geofencingEvent.getGeofenceTransition());
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
                sendEvent(ON_ENTER, enteredGeofences);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "Exit geofence event detected. Sending event.");
                final ArrayList<String> exitingGeofences = new ArrayList<>();
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    exitingGeofences.add(geofence.getRequestId());
                }
                sendEvent(ON_EXIT, exitingGeofences);
                break;
        }
    }

    private void sendEvent(String event, ArrayList<String> params) {
        final LocalBroadcastManager lbManager =
                LocalBroadcastManager.getInstance(this.getApplicationContext());
        final Intent intent = new Intent(RNBoundaryModule.GEOFENCE_DATA_TO_EMIT);
        intent.putExtra("event", event);
        intent.putExtra("params", params);
        lbManager.sendBroadcast(intent);
    }
}