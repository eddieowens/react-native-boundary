package com.eddieowens.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.eddieowens.services.BoundaryEventJobIntentService;

import static com.eddieowens.RNBoundaryModule.TAG;

public class BoundaryEventBroadcastReceiver extends BroadcastReceiver {

    public BoundaryEventBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Broadcasting geofence event");
        JobIntentService.enqueueWork(context, BoundaryEventJobIntentService.class, 0, intent);
    }
}
