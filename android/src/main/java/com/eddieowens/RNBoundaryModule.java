package com.eddieowens;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.eddieowens.services.BoundaryIntentService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class RNBoundaryModule extends ReactContextBaseJavaModule {

    public static final String ON_ENTER = "onEnter";
    public static final String ON_EXIT = "onExit";

    private final ReactApplicationContext reactContext;
    private final GeofencingClient mGeofencingClient;

    private PendingIntent mBoundaryPendingIntent;

    public RNBoundaryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.mGeofencingClient = LocationServices.getGeofencingClient(reactContext);
        GeofenceEventBroadcastReceiver geofenceEventBroadcastReceiver = new GeofenceEventBroadcastReceiver();
        LocalBroadcastManager.getInstance(reactContext).registerReceiver(geofenceEventBroadcastReceiver, new IntentFilter("RNBoundary"));
    }

    @ReactMethod
    public void removeAll() {
        mGeofencingClient.removeGeofences(getBoundaryPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    @ReactMethod
    public void remove(final String boundaryRequestId, Promise promise) {
        removeGeofence(promise, Collections.singletonList(boundaryRequestId));
    }

    @ReactMethod
    public void remove(final ReadableArray readableArray, Promise promise) {

        final List<String> boundaryRequestIds = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); ++i) {
            boundaryRequestIds.add(readableArray.getString(i));
        }

        removeGeofence(promise, boundaryRequestIds);
    }

    @ReactMethod
    public void add(final ReadableMap readableMap, Promise promise) {
        final GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofence(readableMap));
        addGeofence(promise, geofencingRequest, geofencingRequest.getGeofences().get(0).getRequestId());
    }

    @ReactMethod
    public void add(final ReadableArray readableArray, Promise promise) {
        final List<Geofence> geofences = createGeofences(readableArray);
        final WritableArray geofenceRequestIds = Arguments.createArray();
        for (Geofence g : geofences) {
            geofenceRequestIds.pushString(g.getRequestId());
        }

        GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofences(readableArray));

        addGeofence(promise, geofencingRequest, geofenceRequestIds);
    }

    private Geofence createGeofence(ReadableMap readableMap) {
        return new Geofence.Builder()
                .setRequestId(readableMap.getString("id"))
                .setCircularRegion(readableMap.getDouble("lat"), readableMap.getDouble("lng"), (float) readableMap.getDouble("radius"))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private List<Geofence> createGeofences(ReadableArray readableArray) {
        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); ++i) {
            geofences.add(createGeofence(readableArray.getMap(i)));
        }
        return geofences;
    }

    private GeofencingRequest createGeofenceRequest(List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private PendingIntent getBoundaryPendingIntent() {
        if (mBoundaryPendingIntent != null) {
            return mBoundaryPendingIntent;
        }
        Intent intent = new Intent(this.reactContext, BoundaryIntentService.class);
        mBoundaryPendingIntent = PendingIntent.getService(this.reactContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mBoundaryPendingIntent;

    }

    private void addGeofence(final Promise promise, final GeofencingRequest geofencingRequest, final WritableArray geofenceRequestIds) {
        if (ActivityCompat.checkSelfPermission(this.reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            promise.reject("PERM", "Access fine location is not permitted");
        } else {
            mGeofencingClient.addGeofences(
                    geofencingRequest,
                    getBoundaryPendingIntent()
            )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            promise.resolve(geofenceRequestIds);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            promise.reject(e);
                        }
                    });
        }

    }

    private void addGeofence(final Promise promise, final GeofencingRequest geofencingRequest, final String requestId) {
        if (ActivityCompat.checkSelfPermission(this.reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            promise.reject("PERM", "Access fine location is not permitted");
        } else {
            Logger.getLogger(this.getName()).info("add geofence called!!");

            mGeofencingClient.addGeofences(
                    geofencingRequest,
                    getBoundaryPendingIntent()
            )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            promise.resolve(requestId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            promise.reject(e);
                        }
                    });
        }
    }

    private void removeGeofence(final Promise promise, final List<String> requestIds) {
        mGeofencingClient.removeGeofences(requestIds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        promise.resolve(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        promise.reject(e);
                    }
                });
    }

    @Override
    public String getName() {
        return "RNBoundary";
    }

    public class GeofenceEventBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    }

    private void sendEvent(GeofencingEvent geofencingEvent, String event) {
        WritableArray writableArray = Arguments.createArray();
        for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
            writableArray.pushString(geofence.getRequestId());
        }
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, writableArray);
    }
}