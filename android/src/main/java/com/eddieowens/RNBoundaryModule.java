package com.eddieowens;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.eddieowens.receivers.BoundaryEventBroadcastReceiver;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RNBoundaryModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public static final String TAG = "RNBoundary";
    public static final String ON_ENTER = "onEnter";
    public static final String ON_EXIT = "onExit";
    public static final String GEOFENCE_DATA_TO_EMIT = "com.eddieowens.GEOFENCE_DATA_TO_EMIT";

    private GeofencingClient mGeofencingClient;
    private PendingIntent mBoundaryPendingIntent;

    RNBoundaryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mGeofencingClient = LocationServices.getGeofencingClient(getReactApplicationContext());
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @ReactMethod
    public void removeAll(final Promise promise) {
        mGeofencingClient.removeGeofences(getBoundaryPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully removed all geofences");
                        promise.resolve(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to remove all geofences");
                        promise.reject(e);
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
    public void add(final ReadableMap readableMap, final Promise promise) {
        final GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofence(readableMap));
        addGeofence(promise, geofencingRequest, geofencingRequest.getGeofences().get(0).getRequestId());
    }

    @ReactMethod
    public void add(final ReadableArray readableArray, final Promise promise) {
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
        Intent intent = new Intent(getReactApplicationContext(), BoundaryEventBroadcastReceiver.class);
        mBoundaryPendingIntent = PendingIntent.getBroadcast(getReactApplicationContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mBoundaryPendingIntent;
    }

    private void addGeofence(final Promise promise, final GeofencingRequest geofencingRequest, final WritableArray geofenceRequestIds) {
        int permission = ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission != PackageManager.PERMISSION_GRANTED) {
            permission = requestPermissions();
        }


        if (permission != PackageManager.PERMISSION_GRANTED) {
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
        int permission = ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission != PackageManager.PERMISSION_GRANTED) {
            permission = requestPermissions();
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            promise.reject("PERM", "Access fine location is not permitted. Hello");
        } else {
            Log.i(TAG, "Attempting to add geofence.");

            mGeofencingClient.addGeofences(
                    geofencingRequest,
                    getBoundaryPendingIntent()
            )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Successfully added geofence.");
                            promise.resolve(requestId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Failed to add geofence.");
                            promise.reject(e);
                        }
                    });
        }
    }

    private void removeGeofence(final Promise promise, final List<String> requestIds) {
        Log.i(TAG, "Attempting to remove geofence.");
        mGeofencingClient.removeGeofences(requestIds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully removed geofence.");
                        promise.resolve(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to remove geofence.");
                        promise.reject(e);
                    }
                });
    }

    private int requestPermissions() {
        ActivityCompat.requestPermissions(getReactApplicationContext().getCurrentActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1);

        return ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public String getName() {
        return "RNBoundary";
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }
}