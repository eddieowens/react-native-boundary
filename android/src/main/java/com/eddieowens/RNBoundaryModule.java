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
import android.util.Log;
import android.os.Handler;

import com.eddieowens.errors.GeofenceErrorMessages;
import com.eddieowens.services.BoundaryEventIntentService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
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

public class RNBoundaryModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public static final String ON_ENTER = "onEnter";
    public static final String ON_EXIT = "onExit";
    public static final String TAG = "RNBoundary";

    private boolean mHasListeners;

    private GeofencingClient mGeofencingClient;

    private GeofenceEventBroadcastReceiver geofenceEventBroadcastReceiver;

    private PendingIntent mBoundaryPendingIntent;

    public RNBoundaryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
        Log.i(TAG, "construct!");

    }

    @ReactMethod
    public void setHasListeners(final boolean hasListeners, final Promise promise) {
        Log.i(TAG, "hasListeners " + hasListeners);

        mHasListeners = hasListeners;

        // We cannot rely on onHostResume here
        // the phone may be locked and onHostResume is never called
        // it's a better place to register our receiver.
        if (hasListeners) {
          if (geofenceEventBroadcastReceiver == null) {
              geofenceEventBroadcastReceiver = new GeofenceEventBroadcastReceiver();
              LocalBroadcastManager.getInstance(getReactApplicationContext())
                      .registerReceiver(
                              geofenceEventBroadcastReceiver,
                              new IntentFilter(BoundaryEventIntentService.ACTION)
                      );
          }
          if (mGeofencingClient == null) {
              this.mGeofencingClient = LocationServices.getGeofencingClient(getReactApplicationContext().getBaseContext());
          }
        }

        promise.resolve(null);
    }

    @ReactMethod
    public void removeAll(final Promise promise) {
        Log.i(TAG, "removeAll");

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
      Log.i(TAG, "create " + readableMap.getString("id") + ", " +
        readableMap.getDouble("lat") + ", " +
        readableMap.getDouble("lng"));
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
        Intent intent = new Intent(getReactApplicationContext().getBaseContext(), BoundaryEventIntentService.class);
        intent.setAction("com.eddieowens.geofence.ACTION_RECEIVE");
        return PendingIntent.getBroadcast(getReactApplicationContext().getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


    }

    private void addGeofence(final Promise promise, final GeofencingRequest geofencingRequest, final WritableArray geofenceRequestIds) {
        if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            promise.reject("PERM", "Access fine location is not permitted");
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

    private void sendEvent(String event, Object params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }

    public class GeofenceEventBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "Error in handling geofence " + GeofenceErrorMessages.getErrorString(geofencingEvent.getErrorCode()));
                return;
            }

            Log.i(TAG, "onReceive");

            sendGeoFenceWithDelay(geofencingEvent, 0);
        }

        public void sendGeoFenceWithDelay(final GeofencingEvent geofencingEvent, final int retry) {
            if (retry < 5) {
                if (mHasListeners) {
                    sendGeoFence(geofencingEvent);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendGeoFenceWithDelay(geofencingEvent, retry + 1);
                        }
                    }, 2000);
                }
            }
        }

        public void sendGeoFence(GeofencingEvent geofencingEvent) {
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
    }

    @Override
    public String getName() {
        return "RNBoundary";
    }

    @Override
    public void onHostResume() {
        Log.i(TAG, "resume!");
    }

    @Override
    public void onHostPause() {

        Log.i(TAG, "pause!");
    }

    @Override
    public void onHostDestroy() {

        Log.i(TAG, "destroy!");
    }
}
