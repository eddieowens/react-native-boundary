package com.eddieowens.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static com.eddieowens.RNBoundaryModule.ON_ENTER;
import static com.eddieowens.RNBoundaryModule.ON_EXIT;

public class BoundaryIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BoundaryIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent != null) {
            switch (geofencingEvent.getGeofenceTransition()) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    sendEvent(geofencingEvent, ON_ENTER);
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    sendEvent(geofencingEvent, ON_EXIT);
                    break;
            }
        }
    }

    private void sendEvent(GeofencingEvent geofencingEvent, String event) {
        List<String> geofenceIds = new ArrayList<>();
        for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
            geofenceIds.add(geofence.getRequestId());
        }
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(ON_ENTER, geofenceIds);
    }

    private ReactApplicationContext getReactApplicationContext() {
        return (ReactApplicationContext) getApplicationContext();
    }
}