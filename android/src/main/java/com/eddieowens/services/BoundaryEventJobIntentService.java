package com.eddieowens.services;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.eddieowens.errors.GeofenceErrorMessages;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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
                WritableArray writableArray = Arguments.createArray();
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    writableArray.pushString(geofence.getRequestId());
                }
                sendEvent(ON_ENTER, writableArray);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "Exit geofence event detected. Sending event.");
                WritableArray writableArray1 = Arguments.createArray();
                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    writableArray1.pushString(geofence.getRequestId());
                }
                sendEvent(ON_EXIT, writableArray1);
                break;
        }
    }

    private void sendEvent(String event, Object params) {
        Log.i(TAG, "Sending events " + event);
        ((ReactApplicationContext) this.getBaseContext())
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
        Log.i(TAG, "Sent events");
    }
}