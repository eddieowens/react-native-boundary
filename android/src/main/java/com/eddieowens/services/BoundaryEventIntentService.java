package com.eddieowens.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.eddieowens.RNBoundaryModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.eddieowens.RNBoundaryModule.ON_ENTER;
import static com.eddieowens.RNBoundaryModule.ON_EXIT;
import static com.eddieowens.RNBoundaryModule.TAG;

public class BoundaryEventIntentService extends IntentService {

    private static final Logger logger = Logger.getLogger(TAG);

    public static final String ACTION = "RNBoundary.Event";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BoundaryEventIntentService(String name) {
        super(name);
    }

    public BoundaryEventIntentService() {
        super("BoundaryEventIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        if (intent != null) {
            logger.info("Broadcasting event");
            intent.setAction(ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}